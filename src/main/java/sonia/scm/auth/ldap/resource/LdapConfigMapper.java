/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.auth.ldap.resource;

import de.otto.edison.hal.Links;
import org.apache.commons.lang.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.auth.ldap.LdapConfig;
import sonia.scm.config.ConfigurationPermissions;

import jakarta.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static sonia.scm.auth.ldap.resource.LdapModule.PERMISSION_NAME;

@Mapper
public abstract class LdapConfigMapper {

  @SuppressWarnings("squid:S2068") // No, this is definitely no password
  private static final String DUMMY_PASSWORD = "__DUMMY__";

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  @Mapping(ignore = true, target = "attributes")
  public abstract LdapConfigDto map(LdapConfig config);

  public abstract LdapConfig map(LdapConfigDto dto, @Context LdapConfig oldConfig);

  @ObjectFactory
  LdapConfigDto createDto(LdapConfig config) {
    Links.Builder linksBuilder = linkingTo().self(self());
    if (ConfigurationPermissions.write(PERMISSION_NAME).isPermitted()) {
      linksBuilder.single(link("update", update()));
      linksBuilder.single(link("test", test()));
    }
    return new LdapConfigDto(linksBuilder.build());
  }

  private String self() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), LdapConfigResource.class);
    return linkBuilder.method("getConfig").parameters().href();
  }

  private String update() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), LdapConfigResource.class);
    return linkBuilder.method("setConfig").parameters().href();
  }

  private String test() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), LdapConfigResource.class);
    return linkBuilder.method("testConfig").parameters().href();
  }

  @AfterMapping
  void replaceDummyWithOldPassword(@MappingTarget LdapConfig target, @Context LdapConfig oldConfiguration) {
    if (DUMMY_PASSWORD.equals(target.getConnectionPassword())) {
      target.setConnectionPassword(oldConfiguration.getConnectionPassword());
    }
  }

  @AfterMapping
  void replacePasswordWithDummy(@MappingTarget LdapConfigDto target) {
    if (StringUtils.isNotEmpty(target.getConnectionPassword())) {
      target.setConnectionPassword(DUMMY_PASSWORD);
    }
  }
}
