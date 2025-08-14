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
import org.apache.shiro.authc.BearerToken;
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
import sonia.scm.user.UserTestData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
  private LdapAuthenticatorFactory ldapAuthenticatorFactory;

  @Mock
  private LdapAuthenticator ldapAuthenticator;

  @Mock
  private InvalidCredentialsCache invalidCredentialsCache;

  private LdapConfig config;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUpRealm() {
    config = createConfig();
    lenient().when(configStore.get()).thenReturn(config);
    lenient().when(cacheManager.getCache(LdapRealm.CACHE_NAME)).thenReturn(cache);
    lenient().when(ldapAuthenticatorFactory.create(config)).thenReturn(ldapAuthenticator);
    realm = new LdapRealm(configStore, syncingRealmHelper, cacheManager, ldapAuthenticatorFactory, invalidCredentialsCache);
  }

  private void mockAuthenticator() {
    lenient().when(ldapAuthenticator.authenticate("trillian", "trilli123")).thenReturn(
      Optional.of(UserTestData.createTrillian())
    );
    lenient().when(ldapAuthenticator.authenticate(eq("trillian"), not(eq("trilli123")))).thenThrow(
      UserAuthenticationFailedException.class
    );
    lenient().when(ldapAuthenticator.authenticate(not(eq("trillian")), any(String.class))).thenReturn(
      Optional.empty()
    );
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
    mockAuthenticator();

    AuthenticationInfo authenticationInfoMock = mock(AuthenticationInfo.class);
    when(authenticationInfoMock.getCredentials()).thenReturn("trilli123");
    when(syncingRealmHelper.createAuthenticationInfo(eq(LdapRealm.TYPE), any())).thenReturn(authenticationInfoMock);

    AuthenticationInfo authenticationInfo = realm.getAuthenticationInfo(createToken("trillian", "trilli123"));
    verify(syncingRealmHelper).store(any(User.class));
    assertThat(authenticationInfo).isSameAs(authenticationInfoMock);
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldCacheAuthenticationInfo() {
    ldif(1);
    mockAuthenticator();

    AuthenticationInfo authenticationInfoMock = mock(AuthenticationInfo.class);
    when(authenticationInfoMock.getCredentials()).thenReturn("trilli123");
    when(syncingRealmHelper.createAuthenticationInfo(eq(LdapRealm.TYPE), any())).thenReturn(authenticationInfoMock);

    realm.getAuthenticationInfo(createToken("trillian", "trilli123"));
    verify(cache).put("trillian", authenticationInfoMock);
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldReturnAuthenticationInfoFromCache() {
    mockAuthenticator();
    AuthenticationToken token = createToken("trillian", "trilli123");

    AuthenticationInfo authenticationInfoMock = mock(AuthenticationInfo.class);
    when(authenticationInfoMock.getCredentials()).thenReturn("trilli123");
    when(cache.get("trillian")).thenReturn(authenticationInfoMock);

    AuthenticationInfo authenticationInfo = realm.getAuthenticationInfo(token);
    assertThat(authenticationInfo).isSameAs(authenticationInfoMock);
    verifyNoInteractions(ldapAuthenticator);
  }

  @Test
  void testWrongPassword() {
    ldif(1);
    mockAuthenticator();
    AuthenticationToken token = createToken("trillian", "trilli1234");
    assertThrows(UserAuthenticationFailedException.class, () -> realm.doGetAuthenticationInfo(token));
  }

  @Test
  void shouldNotQueryLdapIfInvalidPasswordIsCached() {
    UsernamePasswordToken token = createToken("trillian", "trilli123");
    doThrow(AuthenticationException.class).when(invalidCredentialsCache).verifyNotInvalid(token);

    assertThrows(AuthenticationException.class, () -> realm.getAuthenticationInfo(token));

    verifyNoInteractions(ldapAuthenticatorFactory);
  }

  @Test
  void shouldCacheInvalidCredential() {
    ldif(1);
    UsernamePasswordToken token = createToken("arthur", "trilli123");

    assertThrows(AuthenticationException.class, () -> realm.getAuthenticationInfo(token));

    verify(invalidCredentialsCache).cacheAsInvalid(any());
    verify(cache, never()).put(any(String.class), any(AuthenticationInfo.class));
  }

  @Test
  void shouldThrowExceptionIfTokenClassDoesntMatch() {
    ldif(1);
    AuthenticationToken token = new BearerToken("abcdef");

    assertThrows(IllegalArgumentException.class, () -> realm.getAuthenticationInfo(token));
  }

  private UsernamePasswordToken createToken(String username, String password) {
    return new UsernamePasswordToken(username, password);
  }
}

