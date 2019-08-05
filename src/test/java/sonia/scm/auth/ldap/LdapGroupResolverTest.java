package sonia.scm.auth.ldap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.store.InMemoryConfigurationStore;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LdapGroupResolverTest extends LdapServerTestBaseJunit5 {

  private LdapConfig config;
  private LdapGroupResolver groupResolver;

  @BeforeEach
  void setUpAuthenticator() {
    config = createConfig();
    LdapConfigStore ldapConfigStore = new LdapConfigStore(new InMemoryConfigurationStore<>());
    ldapConfigStore.set(config);
    groupResolver = new LdapGroupResolver(ldapConfigStore);
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
  void shouldThrowConfigurationExceptionIfBaseDNIsNotDefined() {
    config.setBaseDn(null);

    assertThrows(ConfigurationException.class, () -> groupResolver.resolve("trillian"));
  }

}
