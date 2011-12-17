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

  /** Field description */
  public static final String LDIF_002 = "/ldif/002.ldif";

  /** Field description */
  public static final String LDIF_003 = "/ldif/003.ldif";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param ar
   * @param name
   * @param displayName
   * @param mail
   */
  public void assertSuccess(AuthenticationResult ar, String name,
                            String displayName, String mail)
  {
    assertNotNull(ar);
    assertEquals(AuthenticationState.SUCCESS, ar.getState());

    User user = ar.getUser();

    assertNotNull(user);
    assertEquals(LDAPAuthenticationHandler.TYPE, user.getType());
    assertEquals(name, user.getName());
    assertEquals(displayName, user.getDisplayName());
    assertEquals(mail, user.getMail());
  }

  /**
   * Method description
   *
   *
   * @param ar
   */
  public void assertTrillian(AuthenticationResult ar)
  {
    assertSuccess(ar, "trillian", "Tricia McMillan",
                  "tricia.mcmillan@hitchhiker.com");
  }

  /**
   * Method description
   *
   *
   * @param ar
   */
  public void assertZaphod(AuthenticationResult ar)
  {
    assertSuccess(ar, "zaphod", "Zaphod Beeblebrox",
                  "zaphod.beeblebrox@hitchhiker.com");
  }

  /**
   * Method description
   *
   *
   *
   * @throws LDAPException
   */
  @Test
  public void testGroupAttribute() throws LDAPException
  {
    initialize(LDIF_003);

    LDAPConfig config = createConfig();

    config.setUnitGroup("cn=Other Groups");

    LDAPAuthenticationHandler handler = createLDAPAuthHandler(config);
    AuthenticationResult ar = handler.authenticate(null, null, "trillian",
                                "trilli123");

    assertTrillian(ar);

    Collection<String> groups = ar.getGroups();

    assertNotNull(groups);
    assertTrue(groups.size() == 3);
    assertTrue(groups.contains("HeartOfGold"));
    assertTrue(groups.contains("RestaurantAtTheEndOfTheUniverse"));
    assertTrue(groups.contains("HappyVerticalPeopleTransporter"));
  }

  /**
   * Method description
   *
   *
   *
   * @throws LDAPException
   */
  @Test
  public void testGroups() throws LDAPException
  {
    initialize(LDIF_002);

    LDAPAuthenticationHandler handler = createLDAPAuthHandler();
    AuthenticationResult ar = handler.authenticate(null, null, "trillian",
                                "trilli123");

    assertTrillian(ar);

    Collection<String> groups = ar.getGroups();

    assertNotNull(groups);
    assertTrue(groups.size() == 2);
    assertTrue(groups.contains("HeartOfGold"));
    assertTrue(groups.contains("RestaurantAtTheEndOfTheUniverse"));
    ar = handler.authenticate(null, null, "zaphod", "zaphod123");
    assertZaphod(ar);
    groups = ar.getGroups();
    assertNotNull(groups);
    assertTrue(groups.size() == 1);
    assertTrue(groups.contains("HeartOfGold"));
  }

  /**
   * Method description
   *
   *
   *
   * @throws LDAPException
   */
  @Test
  public void testNotFound() throws LDAPException
  {
    initialize(LDIF_001);

    LDAPAuthenticationHandler handler = createLDAPAuthHandler();
    AuthenticationResult ar = handler.authenticate(null, null, "hansolo",
                                "trilli123");

    assertFailed(AuthenticationState.NOT_FOUND, ar);
  }

  /**
   * Method description
   *
   *
   *
   *
   * @throws LDAPException
   */
  @Test
  public void testSimpleAuthentication() throws LDAPException
  {
    initialize(LDIF_001);

    LDAPAuthenticationHandler handler = createLDAPAuthHandler();
    AuthenticationResult ar = handler.authenticate(null, null, "trillian",
                                "trilli123");

    assertTrillian(ar);

    Collection<String> groups = ar.getGroups();

    assertNotNull(groups);
    assertTrue(groups.isEmpty());
  }

  /**
   * Method description
   *
   *
   *
   *
   * @throws LDAPException
   */
  @Test
  public void testWrongPassword() throws LDAPException
  {
    initialize(LDIF_001);

    LDAPAuthenticationHandler handler = createLDAPAuthHandler();
    AuthenticationResult ar = handler.authenticate(null, null, "trillian",
                                "trilli1234");

    assertFailed(AuthenticationState.FAILED, ar);
  }

  /**
   * Method description
   *
   *
   * @param state
   * @param ar
   */
  private void assertFailed(AuthenticationState state, AuthenticationResult ar)
  {
    assertNotNull(ar);
    assertEquals(state, ar.getState());

    User user = ar.getUser();

    assertNull(user);

    Collection<String> groups = ar.getGroups();

    assertNull(groups);
  }
}
