/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.auth.ldap;

//~--- non-JDK imports --------------------------------------------------------

import com.unboundid.ldap.sdk.LDAPException;

import org.junit.Test;

import sonia.scm.web.security.AuthenticationResult;
import sonia.scm.web.security.AuthenticationState;

import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Sdorra
 */
public class LDAPAuthenticationContextTest extends LDAPServerTestBase
{

  /**
   * Method description
   *
   *
   * @throws LDAPException
   * 
   * @see <a hre="https://groups.google.com/d/msg/scmmanager/EJ1k-6BVjxs/XzJpFHq1mMIJ">XzJpFHq1mMIJ</a>
   */
  @Test
  public void testFilterEscaping() throws LDAPException
  {
    initialize(LDIF_001);

    LDAPConfig config = createConfig();

    config.setConnectionPassword(BIND_PWD);
    config.setSearchFilter("(&(uid={0}))");

    LDAPAuthenticationContext context = new LDAPAuthenticationContext(config);
    AuthenticationResult ar = context.authenticate("trillian)(objectClass=top",
                                "trilli123");

    assertNull(context.getState().getException());
    LDAPTestUtil.assertFailed(AuthenticationState.NOT_FOUND, ar);
  }

  /**
   * Method description
   *
   *
   * @throws LDAPException
   */
  @Test
  public void testState() throws LDAPException
  {
    initialize(LDIF_001);

    LDAPConfig config = createConfig();

    config.setConnectionPassword("******");
    config.setSearchFilter("(cn={0})");

    LDAPAuthenticationContext context = new LDAPAuthenticationContext(config);
    AuthenticationResult ar = context.authenticate("trillian", "trilli");

    LDAPTestUtil.assertFailed(AuthenticationState.NOT_FOUND, ar);

    LDAPAuthenticationState state = context.getState();

    assertFalse(state.isBind());
    assertFalse(state.isSearchUser());
    assertFalse(state.isAuthenticateUser());

    // set correct password
    config.setConnectionPassword(BIND_PWD);
    context = new LDAPAuthenticationContext(config);
    ar = context.authenticate("trillian", "trilli");
    LDAPTestUtil.assertFailed(AuthenticationState.NOT_FOUND, ar);
    state = context.getState();
    assertTrue(state.isBind());
    assertFalse(state.isSearchUser());
    assertFalse(state.isAuthenticateUser());
    assertFalse(state.isUserValid());

    // set correct search filter
    config.setSearchFilter("(uid={0})");
    context = new LDAPAuthenticationContext(config);
    ar = context.authenticate("trillian", "trilli");
    LDAPTestUtil.assertFailed(AuthenticationState.FAILED, ar);
    state = context.getState();
    assertTrue(state.isBind());
    assertTrue(state.isSearchUser());
    assertFalse(state.isAuthenticateUser());
    assertFalse(state.isUserValid());

    // set correct password
    context = new LDAPAuthenticationContext(config);
    ar = context.authenticate("trillian", "trilli123");
    LDAPTestUtil.assertTrillian(ar);
    state = context.getState();
    assertTrue(state.isBind());
    assertTrue(state.isSearchUser());
    assertTrue(state.isAuthenticateUser());
    assertTrue(state.isUserValid());

    // not valid
    context = new LDAPAuthenticationContext(config);
    ar = context.authenticate("prefect", "prefi123");
    state = context.getState();
    assertNotNull(state);
    assertTrue(state.isBind());
    assertTrue(state.isSearchUser());
    assertTrue(state.isAuthenticateUser());
    assertFalse(state.isUserValid());
  }
}
