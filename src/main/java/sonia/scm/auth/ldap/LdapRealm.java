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
import sonia.scm.plugin.Extension;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.user.User;

import static com.google.common.base.Preconditions.checkArgument;

@Singleton
@Extension
public class LdapRealm extends AuthenticatingRealm {

  public static final String TYPE = "ldap";

  private static final Logger logger = LoggerFactory.getLogger(LdapRealm.class);

  private final SyncingRealmHelper syncingRealmHelper;
  private final LdapConfigStore configStore;

  @Inject
  public LdapRealm(LdapConfigStore configStore, SyncingRealmHelper syncingRealmHelper) {
    this.configStore = configStore;
    this.syncingRealmHelper = syncingRealmHelper;
    setAuthenticationTokenClass(UsernamePasswordToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
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
