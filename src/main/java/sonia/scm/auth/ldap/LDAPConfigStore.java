package sonia.scm.auth.ldap;

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LDAPConfigStore {

  private final ConfigurationStore<LDAPConfig> configurationStore;

  @Inject
  public LDAPConfigStore(ConfigurationStoreFactory configurationStoreFactory) {
    this(configurationStoreFactory.withType(LDAPConfig.class).withName("ldap").build());
  }

  @VisibleForTesting
  LDAPConfigStore(ConfigurationStore<LDAPConfig> configurationStore) {
    this.configurationStore = configurationStore;
  }

  LDAPConfig get() {
    return configurationStore.get();
  }

  void set(LDAPConfig config) {
    configurationStore.set(config);
  }
}
