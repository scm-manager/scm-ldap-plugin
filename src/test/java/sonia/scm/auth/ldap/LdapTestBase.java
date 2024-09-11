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

import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Sebastian Sdorra
 */
public class LdapTestBase {

  public static final String BASE_DN = "dc=scm-manager,dc=org";
  public static final String BIND_DN = "cn=Directory Manager";
  public static final String BIND_PWD = "scm-manager";
  public static final String HOST = "localhost";
  public static final int PORT = 11389;

  protected LdapConfig createConfig() {
    LdapConfig config = new LdapConfig();

    try {
      config.setEnabled(true);
      config.setBaseDn(BASE_DN);
      config.setAttributeNameId("uid");
      config.setAttributeNameFullname("cn");
      config.setAttributeNameMail("mail");
      config.setAttributeNameGroup("memberOf");
      config.setConnectionDn(BIND_DN);
      config.setConnectionPassword(BIND_PWD);
      String hostUrl = "ldap://" + getInetAddress().getHostName() +
        ":" + PORT;
      config.setHostUrl(hostUrl);
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

  protected InetAddress getInetAddress() throws UnknownHostException {
    return InetAddress.getByName(HOST);
  }
}
