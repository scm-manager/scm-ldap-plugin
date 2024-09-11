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

import sonia.scm.user.User;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

class AuthenticationResult {

    private final AuthenticationFailure failure;
    private final Collection<String> groups;
    private final User user;

    AuthenticationResult(User user, Set<String> groups) {
        this.failure = null;
        this.user = user;
        this.groups = groups;
    }

    AuthenticationResult(AuthenticationFailure failure, User user) {
        this.failure = failure;
        this.user = user;
        this.groups = Collections.emptyList();
    }

  AuthenticationResult(AuthenticationFailure failure) {
    this.failure = failure;
    this.user = null;
    this.groups = Collections.emptyList();
  }

    public Collection<String> getGroups() {
        return groups;
    }

    public Optional<AuthenticationFailure> getFailure() {
        return Optional.ofNullable(failure);
    }

    public Optional<User> getUser() {
        return Optional.ofNullable(user);
    }
}
