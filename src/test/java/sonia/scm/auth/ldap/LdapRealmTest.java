/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

  @Mock
  private CacheManager cacheManager;

  @InjectMocks
  private LdapRealm realm;

  @Mock
  @SuppressWarnings("rawtypes")
  private Cache cache;

  private LdapConfig config;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUpRealm() {
    config = createConfig();
    lenient().when(configStore.get()).thenReturn(config);
    lenient().when(cacheManager.getCache(LdapRealm.CACHE_NAME)).thenReturn(cache);
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

    AuthenticationToken token = createToken("hansolo", "trilli123");
    assertThrows(UnknownAccountException.class, () -> realm.doGetAuthenticationInfo(token));
  }

  @Test
  void shouldReturnAuthenticationInfo() {
    ldif(1);

    AuthenticationInfo authenticationInfoMock = mock(AuthenticationInfo.class);
    when(syncingRealmHelper.createAuthenticationInfo(eq(LdapRealm.TYPE), any())).thenReturn(authenticationInfoMock);

    AuthenticationInfo authenticationInfo = realm.getAuthenticationInfo(createToken("trillian", "trilli123"));
    verify(syncingRealmHelper).store(any(User.class));
    assertThat(authenticationInfo).isSameAs(authenticationInfoMock);
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldCacheAuthenticationInfo() {
    ldif(1);

    AuthenticationInfo authenticationInfoMock = mock(AuthenticationInfo.class);
    when(syncingRealmHelper.createAuthenticationInfo(eq(LdapRealm.TYPE), any())).thenReturn(authenticationInfoMock);

    realm.getAuthenticationInfo(createToken("trillian", "trilli123"));
    verify(cache).put("trillian", authenticationInfoMock);
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldReturnAuthenticationInfoFromCache() {
    AuthenticationToken token = createToken("trillian", "trilli123");

    AuthenticationInfo authenticationInfoMock = mock(AuthenticationInfo.class);
    LdapRealm realm = new LdapRealm(configStore, syncingRealmHelper, cacheManager);
    when(cache.get("trillian")).thenReturn(authenticationInfoMock);

    AuthenticationInfo authenticationInfo = realm.getAuthenticationInfo(token);
    assertThat(authenticationInfo).isSameAs(authenticationInfoMock);
  }

  @Test
  void testWrongPassword() {
    ldif(1);

    AuthenticationToken token = createToken("trillian", "trilli1234");
    assertThrows(UserAuthenticationFailedException.class, () -> realm.doGetAuthenticationInfo(token));
  }

  private AuthenticationToken createToken(String username, String password) {
    return new UsernamePasswordToken(username, password);
  }
}

