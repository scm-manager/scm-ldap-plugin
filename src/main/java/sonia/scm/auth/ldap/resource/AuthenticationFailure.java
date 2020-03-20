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

import com.google.common.base.Throwables;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import sonia.scm.auth.ldap.BindConnectionFailedException;
import sonia.scm.auth.ldap.ConfigurationException;
import sonia.scm.auth.ldap.InvalidUserException;
import sonia.scm.auth.ldap.UserAuthenticationFailedException;
import sonia.scm.auth.ldap.UserSearchFailedException;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
class AuthenticationFailure {

  private final boolean configured;
  private final boolean connected;
  private final boolean userFound;
  private final boolean userAuthenticated;
  private final String exception;


  static AuthenticationFailure userNotFound() {
    return userNotFound((String) null);
  }

  static AuthenticationFailure userNotFound(UserSearchFailedException ex) {
    return userNotFound(Throwables.getStackTraceAsString(ex));
  }

  static AuthenticationFailure userNotFound(String exception) {
    return new AuthenticationFailure(true, true, false, false, exception);
  }

  static AuthenticationFailure connectionFailed(BindConnectionFailedException ex) {
    return new AuthenticationFailure(true, false, false, false, Throwables.getStackTraceAsString(ex));
  }

  static AuthenticationFailure authenticationFailed(UserAuthenticationFailedException ex) {
    return new AuthenticationFailure(true, true, true, false, Throwables.getStackTraceAsString(ex));
  }

  static AuthenticationFailure invalidUser(InvalidUserException ex) {
    return new AuthenticationFailure(true, true, true, true, Throwables.getStackTraceAsString(ex));
  }

  static AuthenticationFailure invalidConfig(ConfigurationException ex) {
    return new AuthenticationFailure(false, false, false, false, Throwables.getStackTraceAsString(ex));
  }
}
