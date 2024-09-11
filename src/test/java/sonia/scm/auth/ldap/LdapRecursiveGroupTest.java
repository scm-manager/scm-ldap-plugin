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

import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import sonia.scm.store.InMemoryConfigurationStore;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class LdapRecursiveGroupTest extends LdapServerTestBaseJunit5 {

  private LdapConfig config;
  private LdapGroupResolver groupResolver;

  @BeforeEach
  void setUpAuthenticator() throws NoSuchAlgorithmException {
    config = createConfig();
    config.setEnableNestedGroups(true);
    LdapConfigStore ldapConfigStore = new LdapConfigStore(new InMemoryConfigurationStore<>());
    ldapConfigStore.set(config);
    groupResolver = new LdapGroupResolver(ldapConfigStore, new LdapConnectionFactory());
  }

  /*
    These tests are copied from LdapGroupResolverTest.java and should still work
   */

  @Test
  void shouldReturnGroupsFromAttributeWithoutRecursive() {
    ldif(6);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse", "HappyVerticalPeopleTransporter");
  }

  @Test
  void shouldReturnEmptyGroupWithoutMemberOfWithoutRecursive() {
    ldif(6);

    Set<String> groups = groupResolver.resolve("zaphod");
    assertThat(groups).isEmpty();
  }

  @Test
  void shouldReturnEmptyForNonExistingUsersWithoutRecursive() {
    ldif(6);

    Set<String> groups = groupResolver.resolve("slarti");
    assertThat(groups).isEmpty();
  }

  @Test
  void shouldReturnGroupsFromSearchWithoutRecursive() {
    ldif(7);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse");
  }

  @Test
  void shouldReturnEmptyCollectionWithoutGroupSearchFilterWithoutRecursive() {
    ldif(7);
    config.setSearchFilterGroup(null);
    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).isEmpty();
  }

  @Test
  void shouldReturnGroupsFromSearchAndAttributesWithoutRecursive() {
    ldif(3);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse", "HappyVerticalPeopleTransporter");
  }

  @Test
  void shouldReturnGroupsFromSearchWithUsernameWithoutRecursive() {
    ldif(8);

    config.setSearchFilterGroup("(&(objectClass=posixGroup)(member={1}))");

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse");
  }

  @Test
  void shouldReturnGroupsFromSearchWithMailWithoutRecursive() {
    ldif(9);

    config.setSearchFilterGroup("(&(objectClass=mailGroup)(member={2}))");

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse");
  }

  @Test
  void shouldReturnEmptyCollectionIfLdapIsDisabledWithoutRecursive() {
    ldif(3);

    config.setEnabled(false);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).isEmpty();
  }

  @Test
  void shouldReturnEmptyOnInvalidConfigurationWithoutRecursive() {
    config.setBaseDn(null);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).isEmpty();
  }

  /*
  These tests include actual recursive group definitions
   */

  @Test
  void shouldReturnGroupsFromMemberOf() {
    ldif(12);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse", "HappyVerticalPeopleTransporter", "RestaurantsOfTheUniverse");
  }

  @Test
  void shouldNotReturnGroupsFromMemberOf() {
    ldif(12);

    Set<String> groups = groupResolver.resolve("dephn");
    assertThat(groups).containsOnly("HeartOfGold");
  }

  @Test
  void shouldReturnGroupsFromUniqueMember() {
    ldif(13);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse", "RestaurantsOfTheUniverse", "RestaurantsAtEarth");
  }

  @Test
  void shouldNotReturnGroupsFromUniqueMember() {
    ldif(13);

    Set<String> groups = groupResolver.resolve("dephn");
    assertThat(groups).containsOnly("HeartOfGold");
  }

  @Test
  @Timeout(value = 1)
  void shouldReturnGroupsFromUniqueMemberWithLoop() {
    ldif(14);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse", "RestaurantAtTheStartOfTheUniverse", "RestaurantsOfTheUniverse");
  }
}
