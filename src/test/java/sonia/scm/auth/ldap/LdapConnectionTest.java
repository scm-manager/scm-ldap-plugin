package sonia.scm.auth.ldap;

//~--- non-JDK imports --------------------------------------------------------

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

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;

import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sebastian Sdorra
 */
public class LdapConnectionTest extends LdapTestBase {

  private static final String LDIF = "/ldif/004.ldif";

  //~--- methods --------------------------------------------------------------

  @Before
  public void startServer() throws Exception {

    // System.setProperty("javax.net.debug", "all");

    accesslogHandler = new AccessLogHandler();

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
        new LDIFReader(LdapConnectionTester.class.getResourceAsStream(LDIF));
      ds.importFromLDIF(false, reader);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }

    ds.startListening();
  }

  /**
   * Method description
   */
  @After
  public void stopServer() {
    ds.shutDown(true);
  }

  /**
   * Method description
   *
   * @throws IOException
   * @throws InterruptedException
   * @throws NamingException
   */
  @Test
  @SuppressWarnings("squid:S2699") // test throws exception if it fails
  public void testTlsConnection() throws NamingException, IOException {
    LdapConfig config = createConfig();

    config.setEnableStartTls(true);

    LdapConnection connection = new LdapConnection(config, sslContext, BIND_DN,
      BIND_PWD);

    connection.close();
  }

  @Test(expected = AuthenticationException.class)
  public void testWithWrongPassword() throws NamingException, IOException {
    LdapConfig config = createConfig();

    config.setEnableStartTls(true);

    LdapConnection connection = new LdapConnection(config, sslContext, BIND_DN,
      "test-123");

    connection.close();
  }

  private SSLContext createSSLContext() throws Exception {
    InputStream input = null;
    OutputStream ouput = null;

    File keystore = tempFolder.newFile("keystore.jks");

    try {
      input =
        LdapConnectionTester.class.getResourceAsStream("/security/keystore.jks");
      ouput = new FileOutputStream(keystore);

      IOUtil.copy(input, ouput);
    } finally {
      IOUtil.close(input);
      IOUtil.close(ouput);
    }

    SSLUtil sslUtil =
      new SSLUtil(new KeyStoreKeyManager(keystore,
        "scm-manager.org".toCharArray()), new TrustAllTrustManager());

    return sslUtil.createSSLContext();
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

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private Handler accesslogHandler;

  private InMemoryDirectoryServer ds;

  private SSLContext sslContext;
}
