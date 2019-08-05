package sonia.scm.auth.ldap;

import com.google.inject.util.Providers;
import sonia.scm.user.User;

import java.util.Optional;
import java.util.Set;

class LdapConnectionTester {

  private final LDAPConfig config;

  LdapConnectionTester(LDAPConfig config) {
    this.config = config;
  }

  AuthenticationResult test(String username, String password) {
    LDAPAuthenticator authenticator = new LDAPAuthenticator(config);
    try {
      Optional<User> optionalUser = authenticator.authenticate(username, password);

      if (!optionalUser.isPresent()) {
        return new AuthenticationResult(AuthenticationFailure.userNotFound());
      }

      LDAPGroupResolver groupResolver = new LDAPGroupResolver(Providers.of(config));
      Set<String> groups = groupResolver.resolve(username);

      return new AuthenticationResult(optionalUser.get(), groups);
    } catch (BindConnectionFailedException ex) {
      return new AuthenticationResult(AuthenticationFailure.connectionFailed(ex));
    } catch (UserAuthenticationFailedException ex) {
      return new AuthenticationResult(AuthenticationFailure.authenticationFailed(ex));
    } catch (InvalidUserException ex) {
      return new AuthenticationResult(AuthenticationFailure.invalidUser(ex), ex.getInvalidUser());
    }
  }
}
