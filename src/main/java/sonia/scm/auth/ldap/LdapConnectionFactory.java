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
import com.google.inject.util.Providers;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class LdapConnectionFactory {

  private final Provider<SSLContext> sslContextProvider;

  @Inject
  public LdapConnectionFactory(Provider<SSLContext> sslContextProvider) {
    this.sslContextProvider = sslContextProvider;
  }

  @VisibleForTesting
  public LdapConnectionFactory() throws NoSuchAlgorithmException {
    this(Providers.of(SSLContext.getDefault()));
  }

  LdapConnection createBindConnection(LdapConfig config) {
    try {
      return new LdapConnection(config, sslContextProvider.get(), config.getConnectionDn(), config.getConnectionPassword());
    } catch (IOException | NamingException ex) {
      throw new BindConnectionFailedException("failed to create bind connection for " + config.getConnectionDn(), ex);
    }
  }

  LdapConnection createUserConnection(LdapConfig config, String userDn, String password) {
    try {
      return new LdapConnection(config, sslContextProvider.get(), userDn, password);
    } catch (IOException | NamingException ex) {
      throw new UserAuthenticationFailedException("failed to authenticate user " + userDn, ex);
    }
  }

}
