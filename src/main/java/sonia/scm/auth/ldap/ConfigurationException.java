package sonia.scm.auth.ldap;

import org.apache.shiro.authc.AuthenticationException;

public class ConfigurationException extends AuthenticationException {

  public ConfigurationException(String message) {
    super(message);
  }
}
