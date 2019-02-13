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
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.web.security.AuthenticationResult;
import sonia.scm.web.security.AuthenticationState;

import static com.google.common.base.Preconditions.checkArgument;

@Singleton
@Extension
public class LDAPAuthenticationHandler extends AuthenticatingRealm {

  public static final String TYPE = "ldap";

  private static final Logger logger = LoggerFactory.getLogger(LDAPAuthenticationHandler.class);

  private final ConfigurationStore<LDAPConfig> store;
  private final SyncingRealmHelper syncingRealmHelper;

  private LDAPConfig config;

  @Inject
  public LDAPAuthenticationHandler(ConfigurationStoreFactory factory, SyncingRealmHelper syncingRealmHelper) {
    store = factory.withType(LDAPConfig.class).withName(TYPE).build();
    this.syncingRealmHelper = syncingRealmHelper;

    config = store.get();

    if (config == null) {
      config = new LDAPConfig();
      store.set(config);
    }

    setAuthenticationTokenClass(UsernamePasswordToken.class);

    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
    checkArgument(token instanceof UsernamePasswordToken, "%s is required", UsernamePasswordToken.class);

    UsernamePasswordToken upt = (UsernamePasswordToken) token;
    String username = upt.getUsername();
    char[] password = upt.getPassword();
    AuthenticationResult authenticationResult = new LDAPAuthenticationContext(config).authenticate(username,
      new String(password));

    if (authenticationResult.getState() == AuthenticationState.SUCCESS) {
      syncingRealmHelper.store(authenticationResult.getUser());
      return syncingRealmHelper.createAuthenticationInfo(TYPE, authenticationResult.getUser(), authenticationResult.getGroups());
    } else {
      return null;
    }
  }

  public void storeConfig() {
    store.set(config);
  }

  public LDAPConfig getConfig() {
    return config;
  }

  public void setConfig(LDAPConfig config) {
    this.config = config;
  }
}
