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

package sonia.scm.auth.ldap.resource;

import sonia.scm.auth.ldap.BindConnectionFailedException;
import sonia.scm.auth.ldap.ConfigurationException;
import sonia.scm.auth.ldap.InvalidUserException;
import sonia.scm.auth.ldap.LdapAuthenticator;
import sonia.scm.auth.ldap.LdapConfig;
import sonia.scm.auth.ldap.LdapConnectionFactory;
import sonia.scm.auth.ldap.LdapGroupResolver;
import sonia.scm.auth.ldap.UserAuthenticationFailedException;
import sonia.scm.auth.ldap.UserSearchFailedException;
import sonia.scm.user.User;

import java.util.Optional;
import java.util.Set;

public class LdapConnectionTester {

  private final LdapConfig config;
  private final LdapConnectionFactory ldapConnectionFactory;

  LdapConnectionTester(LdapConnectionFactory ldapConnectionFactory, LdapConfig config) {
    this.config = config;
    this.ldapConnectionFactory = ldapConnectionFactory;
  }

  AuthenticationResult test(String username, String password) {
    LdapAuthenticator authenticator = new LdapAuthenticator(ldapConnectionFactory, config);
    try {
      Optional<User> optionalUser = authenticator.authenticate(username, password);

      if (!optionalUser.isPresent()) {
        return new AuthenticationResult(AuthenticationFailure.userNotFound());
      }

      LdapGroupResolver groupResolver = LdapGroupResolver.from(ldapConnectionFactory, config);
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
