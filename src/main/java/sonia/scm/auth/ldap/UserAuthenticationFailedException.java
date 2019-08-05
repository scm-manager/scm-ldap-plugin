package sonia.scm.auth.ldap;

import org.apache.shiro.authc.AuthenticationException;

public class UserAuthenticationFailedException extends AuthenticationException {

  public UserAuthenticationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
