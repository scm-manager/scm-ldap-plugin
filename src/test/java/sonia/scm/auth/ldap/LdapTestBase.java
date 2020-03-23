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
