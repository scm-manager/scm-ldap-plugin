package sonia.scm.auth.ldap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.plugin.Extension;
import sonia.scm.web.JsonEnricherBase;
import sonia.scm.web.JsonEnricherContext;

import javax.inject.Inject;
import javax.inject.Provider;

import static java.util.Collections.singletonMap;
import static sonia.scm.auth.ldap.LDAPModule.PERMISSION_NAME;
import static sonia.scm.web.VndMediaType.INDEX;

@Extension
public class IndexLinkEnricher extends JsonEnricherBase {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  public IndexLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStore, ObjectMapper objectMapper) {
    super(objectMapper);
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Override
  public void enrich(JsonEnricherContext context) {
    if (resultHasMediaType(INDEX, context) && ConfigurationPermissions.read(PERMISSION_NAME).isPermitted()) {
      String globalJenkinsConfigUrl = new LinkBuilder(scmPathInfoStore.get().get(), LDAPConfigResource.class)
        .method("getConfig")
        .parameters()
        .href();

      JsonNode hgConfigRefNode = createObject(singletonMap("href", value(globalJenkinsConfigUrl)));

      addPropertyNode(context.getResponseEntity().get("_links"), "ldapConfig", hgConfigRefNode);
    }
  }
}
