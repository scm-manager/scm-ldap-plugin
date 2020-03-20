package sonia.scm.auth.ldap;

//~--- non-JDK imports --------------------------------------------------------

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

//~--- JDK imports ------------------------------------------------------------

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import static org.mockito.Mockito.mock;

/**
 *
 * @author Sebastian Sdorra
 */
public class LdapServerTestBase extends LdapTestBase {
  private static InMemoryDirectoryServer ldapServer;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @AfterClass
  public static void shutdownLDAP() {
    ldapServer.shutDown(true);
  }

  /**
   * Method description
   *
   *
   * @throws LDAPException
   * @throws UnknownHostException
   */
  @BeforeClass
  public static void startLDAP() throws LDAPException, UnknownHostException {
    InMemoryDirectoryServerConfig config =
      new InMemoryDirectoryServerConfig(BASE_DN);

    config.addAdditionalBindCredentials(BIND_DN, BIND_PWD);
    config.setListenerConfigs(new InMemoryListenerConfig("listener-1",
      InetAddress.getByName(HOST), PORT, ServerSocketFactory.getDefault(),
      SocketFactory.getDefault(), null));

    // disable schema check, because of memberOf attribute
    config.setSchema(null);
    ldapServer = new InMemoryDirectoryServer(config);
    ldapServer.startListening();
  }
}
