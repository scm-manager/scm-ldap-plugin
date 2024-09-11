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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.store.InMemoryConfigurationStore;

import java.security.NoSuchAlgorithmException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LdapGroupResolverTest extends LdapServerTestBaseJunit5 {

  private LdapConfig config;
  private LdapGroupResolver groupResolver;

  @BeforeEach
  void setUpAuthenticator() throws NoSuchAlgorithmException {
    config = createConfig();
    LdapConfigStore ldapConfigStore = new LdapConfigStore(new InMemoryConfigurationStore<>());
    ldapConfigStore.set(config);
    groupResolver = new LdapGroupResolver(ldapConfigStore, new LdapConnectionFactory());
  }

  @Test
  void shouldReturnGroupsFromAttribute() {
    ldif(6);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse", "HappyVerticalPeopleTransporter");
  }

  @Test
  void shouldReturnEmptyGroupWithoutMemberOf() {
    ldif(6);

    Set<String> groups = groupResolver.resolve("zaphod");
    assertThat(groups).isEmpty();
  }

  @Test
  void shouldReturnEmptyForNonExistingUsers() {
    ldif(6);

    Set<String> groups = groupResolver.resolve("slarti");
    assertThat(groups).isEmpty();
  }

  @Test
  void shouldReturnGroupsFromSearch() {
    ldif(7);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse");
  }

  @Test
  void shouldReturnEmptyCollectionWithoutGroupSearchFilter() {
    ldif(7);
    config.setSearchFilterGroup(null);
    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).isEmpty();
  }

  @Test
  void shouldReturnGroupsFromSearchAndAttributes() {
    ldif(3);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse", "HappyVerticalPeopleTransporter");
  }

  @Test
  void shouldReturnGroupsFromSearchWithUsername() {
    ldif(8);

    config.setSearchFilterGroup("(&(objectClass=posixGroup)(member={1}))");

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse");
  }

  @Test
  void shouldReturnGroupsFromSearchWithMail() {
    ldif(9);

    config.setSearchFilterGroup("(&(objectClass=mailGroup)(member={2}))");

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse");
  }

  @Test
  void shouldReturnEmptyCollectionIfLdapIsDisabled() {
    ldif(3);

    config.setEnabled(false);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).isEmpty();
  }

  @Test
  void shouldReturnEmptyOnInvalidConfiguration() {
    config.setBaseDn(null);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).isEmpty();
  }

  @Test
  void shouldReplaceIllegalCharacters() {
    config.setRemoveInvalidCharacters(true);
    ldif(15);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("hg_HeartOfGold", "hg_RestaurantAtTheEndOfTheUniverse", "Happy_Vertical_People_Transporter");
  }

  @Test
  void shouldResolveOnlyGroupsOfUnitFromAttribute() {
    config.setExcludeGroupsOutsideUnit(true);
    config.setUnitGroup("ou=Other Groups");
    ldif(15);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("Happy Vertical People Transporter");
  }

  @Test
  void shouldResolveOnlyGroupsOfUnitFromSearchFilter() {
    config.setExcludeGroupsOutsideUnit(true);
    config.setUnitGroup("ou=Other Groups");
    ldif(16);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HappyVerticalPeopleTransporter");
  }

  @Test
  void shouldResolveOnlyGroupsOfUnitFromSubTree() {
    config.setExcludeGroupsOutsideUnit(true);
    config.setUnitGroup("ou=Other Groups");
    ldif(17);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("Happy Vertical People Transporter");
  }
}
