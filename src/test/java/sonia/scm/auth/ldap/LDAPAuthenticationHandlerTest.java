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

import sonia.scm.user.User;
import sonia.scm.web.security.AuthenticationResult;
import sonia.scm.web.security.AuthenticationState;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public class LDAPAuthenticationHandlerTest extends LDAPTestBase
{

  /** Field description */
  public static final String LDIF_001 = "/ldif/001.ldif";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @throws Exception
   */
  @Test
  public void testSimpleAuthentication() throws Exception
  {
    initialize(LDIF_001);

    LDAPAuthenticationHandler handler = createLDAPAuthHandler();
    AuthenticationResult ar = handler.authenticate(null, null, "trillian",
                                "trilli123");

    assertNotNull(ar);
    assertEquals(AuthenticationState.SUCCESS, ar.getState());

    User user = ar.getUser();

    assertNotNull(user);
    assertEquals(LDAPAuthenticationHandler.TYPE, user.getType());
    assertEquals("trillian", user.getName());
    assertEquals("Tricia McMillan", user.getDisplayName());
    assertEquals("tricia.mcmillan@hitchhiker.com", user.getMail());

    Collection<String> groups = ar.getGroups();

    assertNotNull(groups);
    assertTrue(groups.isEmpty());
  }
}
