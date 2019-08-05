package sonia.scm.auth.ldap;

import org.apache.shiro.authc.AuthenticationException;

public class BindConnectionFailedException extends AuthenticationException {

  public BindConnectionFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
