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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.util.Providers;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;

@Singleton
public class LdapAuthenticatorFactory {

  private final LdapConnectionFactory ldapConnectionFactory;

  @Inject
  public LdapAuthenticatorFactory(LdapConnectionFactory ldapConnectionFactory) {
    this.ldapConnectionFactory = ldapConnectionFactory;
  }

  @VisibleForTesting
  protected LdapAuthenticatorFactory() throws NoSuchAlgorithmException {
    this(new LdapConnectionFactory(Providers.of(SSLContext.getDefault())));
  }

  public LdapAuthenticator create(LdapConfig config) {
    return new LdapAuthenticator(ldapConnectionFactory, config);
  }
}
