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
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvalidCredentialsCacheTest {

  @Mock
  private CacheManager cacheManager;
  @Mock
  private Cache<Object, Object> cache;
  private final Map<Object, Object> cacheBackend = new HashMap<>();
  private InvalidCredentialsCache invalidCredentialsCache;


  @BeforeEach
  void setUpCache() throws NoSuchAlgorithmException {
    when(cacheManager.getCache("sonia.scm.ldap.invalidCredentials"))
      .thenReturn(cache);
    lenient().when(cache.contains(any()))
      .thenAnswer(invocation -> cacheBackend.containsKey(invocation.getArgument(0)));
    lenient().when(cache.put(any(), any()))
      .thenAnswer(invocation -> cacheBackend.put(invocation.getArgument(0), invocation.getArgument(1)));
    invalidCredentialsCache = new InvalidCredentialsCache(cacheManager);
  }

  @Test
  void shouldCacheInvalidCredentials() {
    UsernamePasswordToken token = new UsernamePasswordToken("trillian", "incorrect");
    invalidCredentialsCache.cacheAsInvalid(token);

    Assert.assertThrows(AuthenticationException.class, () -> invalidCredentialsCache.verifyNotInvalid(token));
  }

  @Test
  void shouldIgnoreUnknownCredentials() {
    UsernamePasswordToken token = new UsernamePasswordToken("trillian", "incorrect");
    invalidCredentialsCache.cacheAsInvalid(token);

    UsernamePasswordToken otherToken = new UsernamePasswordToken("trillian", "unknown");
    invalidCredentialsCache.verifyNotInvalid(otherToken);
  }
}
