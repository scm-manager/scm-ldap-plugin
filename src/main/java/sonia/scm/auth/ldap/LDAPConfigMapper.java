package sonia.scm.auth.ldap;

import de.otto.edison.hal.Links;
import org.apache.commons.lang.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class LDAPConfigMapper {

  private static final String DUMMY_PASSWORD = "__DUMMY__";

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  public abstract LDAPConfigDto map(LDAPConfig config);

  public abstract LDAPConfig map(LDAPConfigDto dto, @Context LDAPConfig oldConfig);

  @ObjectFactory
  LDAPConfigDto createDto(LDAPConfig config) {
    Links.Builder linksBuilder = linkingTo().self(self());
    if (ConfigurationPermissions.write("ldap").isPermitted()) {
      linksBuilder.single(link("update", update()));
      linksBuilder.single(link("test", test()));
    }
    return new LDAPConfigDto(linksBuilder.build());
  }

  private String self() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), LDAPConfigResource.class);
    return linkBuilder.method("getConfig").parameters().href();
  }

  private String update() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), LDAPConfigResource.class);
    return linkBuilder.method("setConfig").parameters().href();
  }

  private String test() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), LDAPConfigResource.class);
    return linkBuilder.method("testConfig").parameters().href();
  }

  @AfterMapping
  void replaceDummyWithOldPassword(@MappingTarget LDAPConfig target, @Context LDAPConfig oldConfiguration) {
    if (DUMMY_PASSWORD.equals(target.getConnectionPassword())) {
      target.setConnectionPassword(oldConfiguration.getConnectionPassword());
    }
  }

  @AfterMapping
  void replacePasswordWithDummy(@MappingTarget LDAPConfigDto target) {
    if (StringUtils.isNotEmpty(target.getConnectionPassword())) {
      target.setConnectionPassword(DUMMY_PASSWORD);
    }
  }
}
