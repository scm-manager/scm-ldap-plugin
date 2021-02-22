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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.plugin.Extension;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.user.User;

import static com.google.common.base.Preconditions.checkArgument;

@Singleton
@Extension
public class LdapRealm extends AuthenticatingRealm {

  public static final String TYPE = "ldap";

  @VisibleForTesting
  static final String CACHE_NAME = "sonia.scm.ldap.authentication";

  private static final Logger logger = LoggerFactory.getLogger(LdapRealm.class);

  private final SyncingRealmHelper syncingRealmHelper;
  private final LdapConfigStore configStore;

  @Inject
  public LdapRealm(LdapConfigStore configStore, SyncingRealmHelper syncingRealmHelper, CacheManager cacheManager) {
    this.configStore = configStore;
    this.syncingRealmHelper = syncingRealmHelper;
    setAuthenticationTokenClass(UsernamePasswordToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());

    Cache<Object, AuthenticationInfo> cache = cacheManager.getCache(CACHE_NAME);
    setAuthenticationCache(cache);
    setAuthenticationCachingEnabled(true);
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
    LdapConfig config = configStore.get();
    if (!config.isEnabled()) {
      logger.debug("ldap not enabled - skipping authentication");
      return null;
    }

    checkArgument(token instanceof UsernamePasswordToken, "%s is required", UsernamePasswordToken.class);

    UsernamePasswordToken upt = (UsernamePasswordToken) token;
    String username = upt.getUsername();
    char[] password = upt.getPassword();

    LdapAuthenticator authenticator = new LdapAuthenticator(config);
    User user = authenticator.authenticate(username, new String(password))
      .orElseThrow(() -> new UnknownAccountException("could not find account with name " + username));

    syncingRealmHelper.store(user);
    return syncingRealmHelper.createAuthenticationInfo(TYPE, user);
  }
}
