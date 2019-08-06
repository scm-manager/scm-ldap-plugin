/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
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
