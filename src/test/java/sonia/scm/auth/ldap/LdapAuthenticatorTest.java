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
import sonia.scm.user.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LdapAuthenticatorTest extends LdapServerTestBaseJunit5 {

  private LdapConfig config;
  private LdapAuthenticator authenticator;

  @BeforeEach
  void setUpAuthenticator() {
    config = createConfig();
    authenticator = new LdapAuthenticator(config);
  }

  @Test
  void shouldAuthenticateUserTrillian() {
    ldif(1);

    Optional<User> optionalUser = authenticator.authenticate("trillian", "trilli123");
    assertThat(optionalUser).isPresent();

    User trillian = optionalUser.get();
    assertTrillian(trillian);
  }

  @Test
  void shouldReturnEmptyOptional() {
    ldif(1);

    Optional<User> optionalUser = authenticator.authenticate("unknown", "secret");
    assertThat(optionalUser).isEmpty();
  }

  @Test
  void shouldThrowBindConnectionFailedException() {
    ldif(1);
    config.setConnectionPassword("totally wrong");
    assertThrows(BindConnectionFailedException.class, () -> authenticator.authenticate("trillian", "trilli123"));
  }

  @Test
  void shouldThrowUserSearchFailedException() {
    ldif(1);
    config.setUnitPeople("cn=totally wrong");
    assertThrows(UserSearchFailedException.class, () -> authenticator.authenticate("trillian", "trilli123"));
  }

  @Test
  void shouldThrowUserAuthenticationFailedException() {
    ldif(1);
    assertThrows(UserAuthenticationFailedException.class, () -> authenticator.authenticate("trillian", "i_don't_know"));
  }

  @Test
  void shouldThrowConfigurationExceptionIfNoBaseDNWasDefined() {
    config.setBaseDn(null);
    assertThrows(ConfigurationException.class, () -> authenticator.authenticate("trillian", "trilli123"));
  }

  @Test
  void shouldThrowConfigurationExceptionIfUserSearchFilterWasNotDefined() {
    config.setSearchFilter(null);
    assertThrows(ConfigurationException.class, () -> authenticator.authenticate("trillian", "trilli123"));
  }

  @Test
  void shouldThrowConfigurationExceptionIfNoUserIdAttributeMappingWasDefined() {
    config.setAttributeNameId(null);
    assertThrows(ConfigurationException.class, () -> authenticator.authenticate("trillian", "trilli123"));
  }

  @Test
  void shouldReturnUserEvenWithoutMail() {
    ldif(10);

    Optional<User> optionalUser = authenticator.authenticate("trillian", "trilli123");
    assertThat(optionalUser).isPresent();
    assertThat(optionalUser.get().getMail()).isNull();
  }

  @Test
  void shouldOnlySetMailIfValid() {
    ldif(11);

    Optional<User> optionalUser = authenticator.authenticate("trillian", "trilli123");
    assertThat(optionalUser).isPresent();
    assertThat(optionalUser.get().getMail()).isNull();
  }

  @Test
  void shouldReturnUserEvenWithoutDisplayName() {
    ldif(10);

    Optional<User> optionalUser = authenticator.authenticate("zaphod", "zaphod123");
    assertThat(optionalUser).isPresent();
    assertThat(optionalUser.get().getDisplayName()).isEqualTo("zaphod");
  }

  private void assertTrillian(User user) {
    assertThat(user.getType()).isEqualTo("ldap");
    assertThat(user.getName()).isEqualTo("trillian");
    assertThat(user.getDisplayName()).isEqualTo("Tricia McMillan");
    assertThat(user.getMail()).isEqualTo("tricia.mcmillan@hitchhiker.com");
  }

}
