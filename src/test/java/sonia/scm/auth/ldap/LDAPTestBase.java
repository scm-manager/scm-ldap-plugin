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

//~--- non-JDK imports --------------------------------------------------------

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import sonia.scm.store.MemoryStoreFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

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
  public static final int PORT = 11389;

  /** Field description */
  protected static InMemoryDirectoryServer ldapServer;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @AfterClass
  public static void shutdownLDAP()
  {
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
  public static void startLDAP() throws LDAPException, UnknownHostException
  {
    InMemoryDirectoryServerConfig config =
      new InMemoryDirectoryServerConfig(BASE_DN);

    config.addAdditionalBindCredentials(BIND_DN, BIND_PWD);
    config.setListenerConfigs(new InMemoryListenerConfig("listener-1",
            InetAddress.getLocalHost(), PORT, ServerSocketFactory.getDefault(),
            SocketFactory.getDefault(), null));

    // disable schema check, becase of memberOf attribute
    config.setSchema(null);
    ldapServer = new InMemoryDirectoryServer(config);
    ldapServer.startListening();
  }

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

      hostUrl.append(InetAddress.getLocalHost().getHostName());
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
  protected LDAPAuthenticationHandler createLDAPAuthHandler()
  {
    return createLDAPAuthHandler(createConfig());
  }

  /**
   * Method description
   *
   *
   * @param config
   *
   * @return
   */
  protected LDAPAuthenticationHandler createLDAPAuthHandler(LDAPConfig config)
  {
    LDAPAuthenticationHandler handler =
      new LDAPAuthenticationHandler(new MemoryStoreFactory());

    handler.init(null);
    handler.setConfig(config);
    handler.storeConfig();

    return handler;
  }

  /**
   * Method description
   *
   *
   * @param ldif
   *
   * @throws LDAPException
   */
  protected void initialize(String ldif) throws LDAPException
  {
    LDIFReader reader =
      new LDIFReader(LDAPTestBase.class.getResourceAsStream(ldif));

    ldapServer.importFromLDIF(true, reader);
  }
}
