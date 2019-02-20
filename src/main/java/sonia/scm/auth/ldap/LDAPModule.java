package sonia.scm.auth.ldap;

import com.google.inject.AbstractModule;
import org.mapstruct.factory.Mappers;
import sonia.scm.plugin.Extension;

@Extension
public class LDAPModule extends AbstractModule {

  public static final String PERMISSION_NAME = "ldap";

  @Override
  protected void configure() {
    bind(LDAPConfigMapper.class).to(Mappers.getMapper(LDAPConfigMapper.class).getClass());
  }
}
