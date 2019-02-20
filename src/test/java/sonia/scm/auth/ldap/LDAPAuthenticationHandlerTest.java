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
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Test;
import sonia.scm.group.GroupNames;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class LDAPAuthenticationHandlerTest extends LDAPServerTestBase
{

  @Test
  public void testDisabled() throws LDAPException
  {
    initialize(LDIF_001);

    LDAPConfig config = createConfig();

    config.setEnabled(false);

    LDAPAuthenticationHandler handler = createLDAPAuthHandler(config);
    AuthenticationInfo ai = handler.doGetAuthenticationInfo(createToken("trillian",
      "trilli123"));

    assertNull(ai);
  }

  @Test
  public void testGroupAttribute() throws LDAPException
  {
    initialize(LDIF_003);

    LDAPConfig config = createConfig();

    config.setUnitGroup("cn=Other Groups");

    LDAPAuthenticationHandler handler = createLDAPAuthHandler(config);
    AuthenticationInfo ai = handler.doGetAuthenticationInfo(createToken("trillian",
      "trilli123"));

    assertTrillian(ai);

    Collection<String> groups = getGroups(ai);

    assertNotNull(groups);
    assertTrue(groups.size() == 3);
    assertTrue(groups.contains("HeartOfGold"));
    assertTrue(groups.contains("RestaurantAtTheEndOfTheUniverse"));
    assertTrue(groups.contains("HappyVerticalPeopleTransporter"));
  }

  @Test
  public void testGroups() throws LDAPException
  {
    initialize(LDIF_002);

    LDAPAuthenticationHandler handler = createLDAPAuthHandler();
    AuthenticationInfo ai = handler.doGetAuthenticationInfo(createToken("trillian",
      "trilli123"));

    assertTrillian(ai);

    Collection<String> groups = getGroups(ai);

    assertNotNull(groups);
    assertTrue(groups.size() == 2);
    assertTrue(groups.contains("HeartOfGold"));
    assertTrue(groups.contains("RestaurantAtTheEndOfTheUniverse"));
    ai = handler.doGetAuthenticationInfo(createToken("zaphod", "zaphod123"));
    assertZaphod(ai);
    groups = getGroups(ai);
    assertNotNull(groups);
    assertTrue(groups.size() == 1);
    assertTrue(groups.contains("HeartOfGold"));
  }

  @Test
  public void testNotFound() throws LDAPException
  {
    initialize(LDIF_001);

    LDAPAuthenticationHandler handler = createLDAPAuthHandler();
    AuthenticationInfo ai = handler.doGetAuthenticationInfo(createToken("hansolo",
      "trilli123"));

    assertNull(ai);
  }

  @Test
  public void testSimpleAuthentication() throws LDAPException
  {
    initialize(LDIF_001);

    LDAPAuthenticationHandler handler = createLDAPAuthHandler();
    AuthenticationInfo ai = handler.doGetAuthenticationInfo(createToken("trillian",
      "trilli123"));

    assertTrillian(ai);

    Collection<String> groups = getGroups(ai);

    assertNotNull(groups);
    assertTrue(groups.isEmpty());
  }

  @Test
  public void testWrongPassword() throws LDAPException
  {
    initialize(LDIF_001);

    LDAPAuthenticationHandler handler = createLDAPAuthHandler();
    AuthenticationInfo ai = handler.doGetAuthenticationInfo(createToken("trillian",
      "trilli1234"));

    assertNull(ai);
  }

  private Collection<String> getGroups(AuthenticationInfo ai) {
    return ai.getPrincipals().oneByType(GroupNames.class).getCollection();
  }

  private void assertTrillian(AuthenticationInfo ai) {
    assertEquals("trillian", ai.getPrincipals().oneByType(String.class));
  }

  private void assertZaphod(AuthenticationInfo ai) {
    assertEquals("zaphod", ai.getPrincipals().oneByType(String.class));
  }

  private AuthenticationToken createToken(String username, String password) {
    UsernamePasswordToken authenticationToken = mock(UsernamePasswordToken.class);
    when(authenticationToken.getUsername()).thenReturn(username);
    when(authenticationToken.getPassword()).thenReturn(password.toCharArray());
    return authenticationToken;
  }
}
