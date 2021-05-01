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
package sonia.scm.auth.ldap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import sonia.scm.store.InMemoryConfigurationStore;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class LdapRecursiveGroupTest extends LdapServerTestBaseJunit5 {

  private LdapConfig config;
  private LdapGroupResolver groupResolver;

  @BeforeEach
  void setUpAuthenticator() {
    config = createConfig();
    config.setEnableNestedGroups(true);
    LdapConfigStore ldapConfigStore = new LdapConfigStore(new InMemoryConfigurationStore<>());
    ldapConfigStore.set(config);
    groupResolver = new LdapGroupResolver(ldapConfigStore);
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
  @Timeout(value = 1, unit = TimeUnit.SECONDS)
  void shouldReturnGroupsFromUniqueMemberWithLoop() {
    ldif(14);

    Set<String> groups = groupResolver.resolve("trillian");
    assertThat(groups).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse", "RestaurantAtTheStartOfTheUniverse", "RestaurantsOfTheUniverse");
  }
}
