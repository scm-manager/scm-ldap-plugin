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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
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
  private final LdapAuthenticatorFactory ldapAuthenticatorFactory;
  private final InvalidCredentialsCache invalidCredentialsCache;

  @Inject
  public LdapRealm(LdapConfigStore configStore,
                   SyncingRealmHelper syncingRealmHelper,
                   CacheManager cacheManager,
                   LdapAuthenticatorFactory ldapAuthenticatorFactory,
                   InvalidCredentialsCache invalidCredentialsCache) {
    this.configStore = configStore;
    this.syncingRealmHelper = syncingRealmHelper;
    this.ldapAuthenticatorFactory = ldapAuthenticatorFactory;
    this.invalidCredentialsCache = invalidCredentialsCache;

    setAuthenticationTokenClass(UsernamePasswordToken.class);

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

    invalidCredentialsCache.verifyNotInvalid(upt);

    LdapAuthenticator authenticator = ldapAuthenticatorFactory.create(config);
    User user;
    try {
      user = authenticator.authenticate(username, new String(password))
        .orElseThrow(() -> new UnknownAccountException("could not find account with name " + username));
    } catch (AuthenticationException e) {
      invalidCredentialsCache.cacheAsInvalid(upt);
      throw e;
    }

    syncingRealmHelper.store(user);
    /*
    IMPORTANT:
    AuthenticatingRealm requires the password of the user to verify if user credentials are already cached.
    Otherwise, the SimpleCredentialMatcher can't verify already cached credentials without causing a NullPointerException.
    Setting this password needs to happen after storing the user in the syncingRealmHelper because this would cause the override of the user password.
    */
    if(user.getPassword() == null) {
      user.setPassword(new String(password));
    }

    return syncingRealmHelper.createAuthenticationInfo(TYPE, user);
  }
}
