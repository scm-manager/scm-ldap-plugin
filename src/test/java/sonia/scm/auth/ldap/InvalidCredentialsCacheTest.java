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
