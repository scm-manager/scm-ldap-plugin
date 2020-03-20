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
