package sonia.scm.auth.ldap;

import com.google.common.base.Throwables;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
class AuthenticationFailure {

  private final boolean connected;
  private final boolean userFound;
  private final boolean userAuthenticated;
  private final String exception;

  static AuthenticationFailure userNotFound() {
    return new AuthenticationFailure(true, false, false, null);
  }

  static AuthenticationFailure connectionFailed(BindConnectionFailedException ex) {
    return new AuthenticationFailure(false, false, false, Throwables.getStackTraceAsString(ex));
  }

  static AuthenticationFailure authenticationFailed(UserAuthenticationFailedException ex) {
    return new AuthenticationFailure(true, true, false, Throwables.getStackTraceAsString(ex));
  }

  static AuthenticationFailure invalidUser(InvalidUserException ex) {
    return new AuthenticationFailure(true, true, true, Throwables.getStackTraceAsString(ex));
  }
}
