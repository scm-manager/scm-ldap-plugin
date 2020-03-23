/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.auth.ldap.resource;

import de.otto.edison.hal.Links;
import org.apache.commons.lang.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.auth.ldap.LdapConfig;
import sonia.scm.config.ConfigurationPermissions;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static sonia.scm.auth.ldap.resource.LdapModule.PERMISSION_NAME;

@Mapper
public abstract class LdapConfigMapper {

  @SuppressWarnings("squid:S2068") // No, this is definitely no password
  private static final String DUMMY_PASSWORD = "__DUMMY__";

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

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
