package sonia.scm.auth.ldap;

import org.apache.shiro.authc.AuthenticationException;

public class UserSearchFailedException extends AuthenticationException {

  public UserSearchFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
