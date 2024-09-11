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

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldif.LDIFReader;
import com.unboundid.util.ssl.KeyStoreKeyManager;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.auth.ldap.resource.LdapConnectionTester;
import sonia.scm.util.IOUtil;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LdapConnectionTest extends LdapTestBase {

  private static final String LDIF = "/ldif/004.ldif";

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private InMemoryDirectoryServer ds;
  private SSLContext sslContext;

  @Before
  public void startServer() throws Exception {

    // System.setProperty("javax.net.debug", "all");

    Handler accesslogHandler = new AccessLogHandler();

    this.sslContext = createSSLContext();

    InMemoryDirectoryServerConfig config =
      new InMemoryDirectoryServerConfig(BASE_DN);

    config.setAccessLogHandler(accesslogHandler);
    config.setAuthenticationRequiredOperationTypes();

    config.setListenerConfigs(
      InMemoryListenerConfig.createLDAPConfig(
        "tls-listener-1", getInetAddress(), PORT,
        sslContext.getSocketFactory()));
    config.addAdditionalBindCredentials(BIND_DN, BIND_PWD);

    ds = new InMemoryDirectoryServer(config);

    LDIFReader reader = null;

    try {
      reader =
        new LDIFReader(Objects.requireNonNull(LdapConnectionTester.class.getResourceAsStream(LDIF)));
      ds.importFromLDIF(false, reader);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }

    ds.startListening();
  }

  @After
  public void stopServer() {
    ds.shutDown(true);
  }

  @Test
  @SuppressWarnings("squid:S2699") // test throws exception if it fails
  public void testTlsConnection() throws Exception {
    LdapConfig config = createConfig();

    config.setEnableStartTls(true);

    LdapConnection connection = new LdapConnection(config, sslContext, BIND_DN, BIND_PWD);

    connection.close();
  }

  @Test(expected = AuthenticationException.class)
  public void testWithWrongPassword() throws NamingException, IOException {
    LdapConfig config = createConfig();

    config.setEnableStartTls(true);

    LdapConnection connection = new LdapConnection(config, sslContext, BIND_DN, "test-123");

    connection.close();
  }

  private SSLContext createSSLContext() throws Exception {
    InputStream input = null;
    OutputStream ouput = null;

    File keystore = tempFolder.newFile("keystore.jks");

    try {
      input = LdapConnectionTester.class.getResourceAsStream("/security/keystore.jks");
      ouput = new FileOutputStream(keystore);

      if (input != null) {
        IOUtil.copy(input, ouput);
      }
    } finally {
      IOUtil.close(input);
      IOUtil.close(ouput);
    }

    SSLUtil sslUtil =
      new SSLUtil(new KeyStoreKeyManager(keystore,
        "scm-manager.org".toCharArray()), new TrustAllTrustManager());

    // TLS 1.0 + 1.1 are deprecated with newer jdk 1.8 versions so we have to use 1.2 or above
    SSLContext sslContext = sslUtil.createSSLContext("TLSv1.2");
    SSLContext.setDefault(sslContext);

    return sslContext;
  }

  //~--- inner classes --------------------------------------------------------

  private static class AccessLogHandler extends Handler {

    @Override
    public void close() throws SecurityException {
    }

    @Override
    public void flush() {
    }

    @Override
    public void publish(LogRecord record) {
      String msg = record.getMessage();
      int index = msg.indexOf("] ");

      if (index > 0) {
        msg = msg.substring(index + 2);
      }

      System.out.println(msg);
    }
  }


  //~--- fields ---------------------------------------------------------------

}
