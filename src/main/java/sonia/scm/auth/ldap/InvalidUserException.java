package sonia.scm.auth.ldap;

import org.apache.shiro.authc.AuthenticationException;
import sonia.scm.user.User;

public class InvalidUserException extends AuthenticationException {

  private final User invalidUser;

  public InvalidUserException(String message, User invalidUser) {
    super(message);
    this.invalidUser = invalidUser;
  }

  public User getInvalidUser() {
    return invalidUser;
  }
}
