package sonia.scm.auth.ldap;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

class LDAPServerTestBaseJunit5 extends LDAPTestBase {

  static final String LDIF_001 = "/ldif/001.ldif";
  static final String LDIF_002 = "/ldif/002.ldif";
  static final String LDIF_003 = "/ldif/003.ldif";

  protected static InMemoryDirectoryServer ldapServer;

  //~--- methods --------------------------------------------------------------

  @AfterAll
  static void shutdownLDAP() {
    ldapServer.shutDown(true);
  }

  @BeforeAll
  public static void startLDAP() throws LDAPException, UnknownHostException {
    InMemoryDirectoryServerConfig config =
      new InMemoryDirectoryServerConfig(BASE_DN);

    config.addAdditionalBindCredentials(BIND_DN, BIND_PWD);
    config.setListenerConfigs(new InMemoryListenerConfig("listener-1",
      InetAddress.getByName(HOST), PORT, ServerSocketFactory.getDefault(),
      SocketFactory.getDefault(), null));

    // disable schema check, becase of memberOf attribute
    config.setSchema(null);
    ldapServer = new InMemoryDirectoryServer(config);
    ldapServer.startListening();
  }


  protected void ldif(int number) {
    String ldif = String.format("/ldif/%03d.ldif", number);
    try (InputStream stream = LDAPServerTestBase.class.getResourceAsStream(ldif)) {
      LDIFReader reader = new LDIFReader(stream);

      ldapServer.importFromLDIF(true, reader);
    } catch (LDAPException | IOException ex) {
      Assertions.fail("failed to load ldif " + ldif, ex);
    }
  }

}
