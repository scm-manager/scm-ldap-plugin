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

package sonia.scm.auth.ldap;

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

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
