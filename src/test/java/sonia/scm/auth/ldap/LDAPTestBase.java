/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
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
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.auth.ldap;

//~--- JDK imports ------------------------------------------------------------

import sonia.scm.store.MemoryStoreFactory;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;

import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class LDAPTestBase
{

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
  protected LDAPConfig createConfig()
  {
    LDAPConfig config = new LDAPConfig();

    try
    {
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
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }

    return config;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  protected StoreFactory createConfigStoreFactory()
  {
    LDAPConfig config = createConfig();
    StoreFactory storeFactory = new MemoryStoreFactory();
    Store store = storeFactory.getStore(LDAPConfig.class, LDAPAuthenticationHandler.TYPE);
    store.set(config);

    return storeFactory;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  protected LDAPConfigList createConfigList(LDAPConfig config)
  {
    LDAPConfigList configList = new LDAPConfigList();
    List<LDAPConfig> configs = new ArrayList<LDAPConfig>();

    configs.add(config);
    configList.setLDAPConfigList(configs);

    return configList;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  protected StoreFactory createConfigListStoreFactory()
  {
    LDAPConfig config = createConfig();
    LDAPConfigList configList = new LDAPConfigList();
    List<LDAPConfig> configs = new ArrayList<LDAPConfig>();

    configs.add(config);
    configList.setLDAPConfigList(configs);

    StoreFactory storeFactory = new MemoryStoreFactory();
    Store store = storeFactory.getStore(LDAPConfigList.class, LDAPAuthenticationHandler.TYPE);
    store.set(configList);

    return storeFactory;
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
  protected InetAddress getInetAddress() throws UnknownHostException
  {
    return InetAddress.getByName(HOST);
  }
}
