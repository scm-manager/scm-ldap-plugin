package sonia.scm.auth.ldap;

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class LdapConfigStore implements Provider<LdapConfig> {

  private final ConfigurationStore<LdapConfig> configurationStore;

  @Inject
  public LdapConfigStore(ConfigurationStoreFactory configurationStoreFactory) {
    this(configurationStoreFactory.withType(LdapConfig.class).withName("ldap").build());
  }

  @VisibleForTesting
  LdapConfigStore(ConfigurationStore<LdapConfig> configurationStore) {
    this.configurationStore = configurationStore;
  }

  public LdapConfig get() {
    return configurationStore.getOptional().orElse(new LdapConfig());
  }

  public void set(LdapConfig config) {
    configurationStore.set(config);
  }
}
