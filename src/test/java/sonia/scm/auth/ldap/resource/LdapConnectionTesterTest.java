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

package sonia.scm.auth.ldap.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.auth.ldap.LdapConfig;
import sonia.scm.auth.ldap.LdapConnectionFactory;
import sonia.scm.auth.ldap.LdapServerTestBaseJunit5;

import javax.net.ssl.SSLContext;

import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

class LdapConnectionTesterTest extends LdapServerTestBaseJunit5 {

  private LdapConfig config;
  private LdapConnectionTester tester;
  private SSLContext sslContext;

  @BeforeEach
  void setUpConnectionTester() throws NoSuchAlgorithmException {
    config = createConfig();
    tester = new LdapConnectionTester(new LdapConnectionFactory(), config);
  }

  @Test
  void shouldReturnSuccessful() {
    ldif(3);

    AuthenticationResult result = tester.test("trillian", "trilli123");
    assertThat(result.getFailure()).isEmpty();
    assertThat(result.getUser()).hasValueSatisfying(r -> assertThat(r.getName()).isEqualTo("trillian"));
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
    assertThat(result.getFailure()).isPresent();
    AuthenticationFailure failure = result.getFailure().get();
    assertThat(failure.isConnected()).isFalse();
    assertThat(failure.getException()).isNotEmpty();
  }

  @Test
  void shouldReturnAuthenticationFailedFailure() {
    ldif(3);

    AuthenticationResult result = tester.test("trillian", "trilli1234");
    assertThat(result.getFailure()).isPresent();
    AuthenticationFailure failure = result.getFailure().get();
    assertThat(failure.isConnected()).isTrue();
    assertThat(failure.isUserFound()).isTrue();
    assertThat(failure.isUserAuthenticated()).isFalse();
    assertThat(failure.getException()).isNotEmpty();
  }

  @Test
  void shouldReturnInvalidConfiguration() {
    ldif(5);

    config.setAttributeNameId(null);

    AuthenticationResult result = tester.test("trillian", "trilli123");
    assertThat(result.getFailure()).isPresent();
    AuthenticationFailure failure = result.getFailure().get();
    assertThat(failure.isConfigured()).isFalse();
    assertThat(failure.isConnected()).isFalse();
    assertThat(failure.isUserFound()).isFalse();
    assertThat(failure.isUserAuthenticated()).isFalse();
    assertThat(failure.getException()).isNotEmpty();
  }

  @Test
  void shouldReturnUserNotFoundFailureWithUnknownBaseDN() {
    ldif(3);

    config.setBaseDn("dc=invalid,dc=org");

    AuthenticationResult result = tester.test("trillain", "trilli123");
    assertThat(result.getFailure()).isPresent();
    AuthenticationFailure failure = result.getFailure().get();
    assertThat(failure.isConfigured()).isTrue();
    assertThat(failure.isConnected()).isTrue();
    assertThat(failure.isUserFound()).isFalse();
    assertThat(failure.isUserAuthenticated()).isFalse();
    assertThat(failure.getException()).isNotEmpty();
  }

}
