package sonia.scm.auth.ldap;

import sonia.scm.group.GroupResolver;
import sonia.scm.plugin.Extension;

import java.util.Set;

@Extension
public class LDAPGroupResolver implements GroupResolver {

  @Override
  public Set<String> resolve(String principal) {
    return null;
  }

}
