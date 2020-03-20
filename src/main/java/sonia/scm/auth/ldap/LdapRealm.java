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
