package sonia.scm.auth.ldap;

import org.apache.shiro.authc.AuthenticationException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UserSearchFailedException extends AuthenticationException {

  public UserSearchFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
