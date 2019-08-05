/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
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
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.auth.ldap;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
@ExtendWith(MockitoExtension.class)
class LdapRealmTest extends LdapServerTestBaseJunit5 {

  @Mock
  private LdapConfigStore configStore;

  @Mock
  private SyncingRealmHelper syncingRealmHelper;

  @InjectMocks
  private LdapRealm realm;

  private LdapConfig config;

  @BeforeEach
  void setUpRealm() {
    config = createConfig();
    when(configStore.get()).thenReturn(config);
  }

  @Test
  void shouldReturnNullIfLdapIsDisabled() {
    ldif(1);
    config.setEnabled(false);

    AuthenticationInfo authenticationInfo = realm.doGetAuthenticationInfo(createToken("trillian", "trilli123"));

    assertThat(authenticationInfo).isNull();
  }

  @Test
  void shouldThrowUnknownAccountException() {
    ldif(1);

    assertThrows(UnknownAccountException.class, () -> realm.doGetAuthenticationInfo(createToken("hansolo", "trilli123")));
  }

  @Test
  void testSimpleAuthentication() {
    ldif(1);

    AuthenticationInfo authenticationInfoMock = mock(AuthenticationInfo.class);
    when(syncingRealmHelper.createAuthenticationInfo(eq(LdapRealm.TYPE), any())).thenReturn(authenticationInfoMock);

    AuthenticationInfo authenticationInfo = realm.doGetAuthenticationInfo(createToken("trillian", "trilli123"));
    verify(syncingRealmHelper).store(any(User.class));
    assertThat(authenticationInfo).isSameAs(authenticationInfoMock);
  }

  @Test
  void testWrongPassword() {
    ldif(1);

    assertThrows(UserAuthenticationFailedException.class, () -> realm.doGetAuthenticationInfo(createToken("trillian", "trilli1234")));
  }

  private AuthenticationToken createToken(String username, String password) {
    return new UsernamePasswordToken(username, password);
  }
}

