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
