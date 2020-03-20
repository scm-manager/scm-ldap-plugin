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
