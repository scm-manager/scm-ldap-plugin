package sonia.scm.auth.ldap.resource;

import com.google.inject.AbstractModule;
import org.mapstruct.factory.Mappers;
import sonia.scm.plugin.Extension;

@Extension
public class LdapModule extends AbstractModule {

  public static final String PERMISSION_NAME = "ldap";

  @Override
  protected void configure() {
    bind(LdapConfigMapper.class).to(Mappers.getMapper(LdapConfigMapper.class).getClass());
  }
}
