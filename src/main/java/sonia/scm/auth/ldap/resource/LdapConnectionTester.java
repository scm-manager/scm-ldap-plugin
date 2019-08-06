package sonia.scm.auth.ldap.resource;

import sonia.scm.auth.ldap.BindConnectionFailedException;
import sonia.scm.auth.ldap.ConfigurationException;
import sonia.scm.auth.ldap.InvalidUserException;
import sonia.scm.auth.ldap.LdapAuthenticator;
import sonia.scm.auth.ldap.LdapConfig;
import sonia.scm.auth.ldap.LdapGroupResolver;
import sonia.scm.auth.ldap.UserAuthenticationFailedException;
import sonia.scm.auth.ldap.UserSearchFailedException;
import sonia.scm.user.User;

import java.util.Optional;
import java.util.Set;

public class LdapConnectionTester {

  private final LdapConfig config;

  LdapConnectionTester(LdapConfig config) {
    this.config = config;
  }

  AuthenticationResult test(String username, String password) {
    LdapAuthenticator authenticator = new LdapAuthenticator(config);
    try {
      Optional<User> optionalUser = authenticator.authenticate(username, password);

      if (!optionalUser.isPresent()) {
        return new AuthenticationResult(AuthenticationFailure.userNotFound());
      }

      LdapGroupResolver groupResolver = LdapGroupResolver.from(config);
      Set<String> groups = groupResolver.resolve(username);

      return new AuthenticationResult(optionalUser.get(), groups);
    } catch (BindConnectionFailedException ex) {
      return new AuthenticationResult(AuthenticationFailure.connectionFailed(ex));
    } catch (UserAuthenticationFailedException ex) {
      return new AuthenticationResult(AuthenticationFailure.authenticationFailed(ex));
    } catch (InvalidUserException ex) {
      return new AuthenticationResult(AuthenticationFailure.invalidUser(ex), ex.getInvalidUser());
    } catch (ConfigurationException ex) {
      return new AuthenticationResult(AuthenticationFailure.invalidConfig(ex));
    } catch (UserSearchFailedException ex) {
      return new AuthenticationResult(AuthenticationFailure.userNotFound(ex));
    }
  }
}
