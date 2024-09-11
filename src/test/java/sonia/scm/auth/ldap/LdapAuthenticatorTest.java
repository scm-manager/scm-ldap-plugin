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
import sonia.scm.user.User;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LdapAuthenticatorTest extends LdapServerTestBaseJunit5 {

  private LdapConfig config;
  private LdapAuthenticator authenticator;

  @BeforeEach
  void setUpAuthenticator() throws NoSuchAlgorithmException {
    config = createConfig();
    authenticator = new LdapAuthenticator(new LdapConnectionFactory(), config);
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
    assertThat(user.getName()).isEqualTo("trillian");
    assertThat(user.getDisplayName()).isEqualTo("Tricia McMillan");
    assertThat(user.getMail()).isEqualTo("tricia.mcmillan@hitchhiker.com");
  }

}
