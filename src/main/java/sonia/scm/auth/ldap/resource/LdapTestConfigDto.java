package sonia.scm.auth.ldap.resource;

//~--- JDK imports ------------------------------------------------------------

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Sebastian Sdorra
 */
@Getter
@Setter
public class LdapTestConfigDto
{
  private LdapConfigDto config;
  private String password;
  private String username;
}
