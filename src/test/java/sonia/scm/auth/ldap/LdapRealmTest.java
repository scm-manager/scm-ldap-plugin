/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.auth.ldap;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.user.User;

import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LdapRealmTest extends LdapServerTestBaseJunit5 {

  @Mock
  private LdapConfigStore configStore;

  @Mock
  private SyncingRealmHelper syncingRealmHelper;

  @Mock
  private CacheManager cacheManager;

  private LdapRealm realm;

  @Mock
  @SuppressWarnings("rawtypes")
  private Cache cache;

  @Spy
  private LdapConnectionFactory ldapConnectionFactory;

  @Mock
  private InvalidCredentialsCache invalidCredentialsCache;

  private LdapConfig config;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUpRealm() {
    config = createConfig();
    lenient().when(configStore.get()).thenReturn(config);
    lenient().when(cacheManager.getCache(LdapRealm.CACHE_NAME)).thenReturn(cache);
    realm = new LdapRealm(configStore, syncingRealmHelper, cacheManager, ldapConnectionFactory, invalidCredentialsCache);
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
  void shouldReturnAuthenticationInfoFromCache() throws NoSuchAlgorithmException {
    AuthenticationToken token = createToken("trillian", "trilli123");

    AuthenticationInfo authenticationInfoMock = mock(AuthenticationInfo.class);
    LdapRealm realm = new LdapRealm(configStore, syncingRealmHelper, cacheManager, new LdapConnectionFactory(), invalidCredentialsCache);
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

  @Test
  void shouldNotQueryLdapIfInvalidPasswordIsCached() {
    UsernamePasswordToken token = createToken("trillian", "trilli123");
    doThrow(AuthenticationException.class).when(invalidCredentialsCache).verifyNotInvalid(token);

    assertThrows(AuthenticationException.class, () -> realm.getAuthenticationInfo(token));

    verifyNoInteractions(ldapConnectionFactory);
  }

  @Test
  void shouldCacheInvalidCredential() {
    ldif(1);
    UsernamePasswordToken token = createToken("arthur", "trilli123");

    assertThrows(AuthenticationException.class, () -> realm.getAuthenticationInfo(token));

    verify(invalidCredentialsCache).cacheAsInvalid(any());
  }

  private UsernamePasswordToken createToken(String username, String password) {
    return new UsernamePasswordToken(username, password);
  }
}

