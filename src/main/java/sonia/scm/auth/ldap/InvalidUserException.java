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

import sonia.scm.user.User;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidUserException extends LdapException {

  private final User invalidUser;

  public InvalidUserException(String message, User invalidUser) {
    super(message);
    this.invalidUser = invalidUser;
  }

  public User getInvalidUser() {
    return invalidUser;
  }
}
