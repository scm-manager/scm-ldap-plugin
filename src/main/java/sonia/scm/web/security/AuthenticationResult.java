package sonia.scm.web.security;

import sonia.scm.user.User;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class AuthenticationResult {
    public static final AuthenticationResult NOT_FOUND = new AuthenticationResult(AuthenticationState.NOT_FOUND);
    public static final AuthenticationResult FAILED = new AuthenticationResult(AuthenticationState.FAILED);

    private final AuthenticationState state;
    private final Collection<String> groups;
    private final User user;

    public AuthenticationResult(User user, Set<String> groups) {
        this.state = AuthenticationState.SUCCESS;
        this.user = user;
        this.groups = groups;
    }

    public AuthenticationResult(AuthenticationState state) {
        this.state = state;
        this.user = null;
        this.groups = Collections.emptyList();
    }

    public Collection<String> getGroups() {
        return groups;
    }

    public AuthenticationState getState() {
        return state;
    }

    public User getUser() {
        return user;
    }
}
