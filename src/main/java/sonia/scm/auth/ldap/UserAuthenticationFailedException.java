package sonia.scm.auth.ldap;

import org.apache.shiro.authc.AuthenticationException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UserAuthenticationFailedException extends AuthenticationException {

  public UserAuthenticationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
