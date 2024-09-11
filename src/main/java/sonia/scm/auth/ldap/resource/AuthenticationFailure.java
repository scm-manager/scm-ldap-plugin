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
