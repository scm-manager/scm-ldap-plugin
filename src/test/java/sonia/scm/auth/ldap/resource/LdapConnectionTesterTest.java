package sonia.scm.auth.ldap.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.auth.ldap.LdapConfig;
import sonia.scm.auth.ldap.LdapServerTestBaseJunit5;

import static org.assertj.core.api.Assertions.assertThat;

class LdapConnectionTesterTest extends LdapServerTestBaseJunit5 {

  private LdapConfig config;
  private LdapConnectionTester tester;

  @BeforeEach
  void setUpConnectionTester() {
    config = createConfig();
    tester = new LdapConnectionTester(config);
  }

  @Test
  void shouldReturnSuccessful() {
    ldif(3);

    AuthenticationResult result = tester.test("trillian", "trilli123");
    assertThat(result.getFailure()).isEmpty();
    assertThat(result.getUser().get().getName()).isEqualTo("trillian");
    assertThat(result.getGroups()).containsOnly("HeartOfGold", "RestaurantAtTheEndOfTheUniverse", "HappyVerticalPeopleTransporter");
  }

  @Test
  void shouldReturnUserNotFoundFailure() {
    ldif(3);

    AuthenticationResult result = tester.test("hansolo", "trilli123");
    assertThat(result.getFailure()).contains(AuthenticationFailure.userNotFound());
  }

  @Test
  void shouldReturnConnectionFailedFailure() {
    config.setConnectionPassword("nonono");

    AuthenticationResult result = tester.test("trillian", "trilli123");
    AuthenticationFailure failure = result.getFailure().get();
    assertThat(failure.isConnected()).isFalse();
    assertThat(failure.getException()).isNotEmpty();
  }

  @Test
  void shouldReturnAuthenticationFailedFailure() {
    ldif(3);

    AuthenticationResult result = tester.test("trillian", "trilli1234");
    AuthenticationFailure failure = result.getFailure().get();
    assertThat(failure.isConnected()).isTrue();
    assertThat(failure.isUserFound()).isTrue();
    assertThat(failure.isUserAuthenticated()).isFalse();
    assertThat(failure.getException()).isNotEmpty();
  }

  @Test
  void shouldReturnInvalidUser() {
    ldif(5);

    AuthenticationResult result = tester.test("trillian", "trilli123");
    AuthenticationFailure failure = result.getFailure().get();
    assertThat(failure.isConnected()).isTrue();
    assertThat(failure.isUserFound()).isTrue();
    assertThat(failure.isUserAuthenticated()).isTrue();
    assertThat(failure.getException()).isNotEmpty();
    assertThat(result.getUser().get().isValid()).isFalse();
  }


}
