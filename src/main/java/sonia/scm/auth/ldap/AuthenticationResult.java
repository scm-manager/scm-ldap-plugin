package sonia.scm.auth.ldap;

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
