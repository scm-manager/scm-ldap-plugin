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
  void shouldReturnInvalidConfiguration() {
    ldif(5);

    config.setAttributeNameId(null);

    AuthenticationResult result = tester.test("trillian", "trilli123");
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
    AuthenticationFailure failure = result.getFailure().get();
    assertThat(failure.isConfigured()).isTrue();
    assertThat(failure.isConnected()).isTrue();
    assertThat(failure.isUserFound()).isFalse();
    assertThat(failure.isUserAuthenticated()).isFalse();
    assertThat(failure.getException()).isNotEmpty();
  }

}
