package sonia.scm.auth.ldap;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author Sebastian Sdorra
 */
public class LdapTestBase {

  /** Field description */
  public static final String BASE_DN = "dc=scm-manager,dc=org";

  /** Field description */
  public static final String BIND_DN = "cn=Directory Manager";

  /** Field description */
  public static final String BIND_PWD = "scm-manager";

  /** Field description */
  public static final String HOST = "localhost";

  /** Field description */
  public static final int PORT = 11389;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected LdapConfig createConfig() {
    LdapConfig config = new LdapConfig();

    try {
      StringBuilder hostUrl = new StringBuilder("ldap://");

      hostUrl.append(getInetAddress().getHostName());
      hostUrl.append(":").append(String.valueOf(PORT));
      config.setEnabled(true);
      config.setBaseDn(BASE_DN);
      config.setAttributeNameId("uid");
      config.setAttributeNameFullname("cn");
      config.setAttributeNameMail("mail");
      config.setAttributeNameGroup("memberOf");
      config.setConnectionDn(BIND_DN);
      config.setConnectionPassword(BIND_PWD);
      config.setHostUrl(hostUrl.toString());
      config.setSearchFilter("(uid={0})");
      config.setSearchFilterGroup("(uniqueMember={0})");
      config.setSearchScope("sub");
      config.setUnitGroup("ou=Groups");
      config.setUnitPeople("ou=People");
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    return config;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws UnknownHostException
   */
  protected InetAddress getInetAddress() throws UnknownHostException {
    return InetAddress.getByName(HOST);
  }
}
