package sonia.scm.auth.ldap;

import org.apache.shiro.authc.AuthenticationException;

public class InvalidUserException extends AuthenticationException {

  public InvalidUserException(String message) {
    super(message);
  }
}
