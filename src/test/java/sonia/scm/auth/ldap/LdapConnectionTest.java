/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



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

/**
 *
 * @author Sebastian Sdorra
 */
public class LdapConnectionTest extends LDAPTestBase
{

  /** Field description */
  private static final String LDIF = "/ldif/004.ldif";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   *
   * @throws Exception
   */
  @Before
  public void startServer() throws Exception
  {

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

    try
    {
      reader =
        new LDIFReader(LdapConnectionTest.class.getResourceAsStream(LDIF));
      ds.importFromLDIF(false, reader);
    }
    finally
    {
      if (reader != null)
      {
        reader.close();
      }
    }

    ds.startListening();
  }

  /**
   * Method description
   *
   */
  @After
  public void stopServer()
  {
    ds.shutDown(true);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws InterruptedException
   * @throws NamingException
   */
  @Test
  public void testTlsConnection() throws NamingException, IOException
  {
    LDAPConfig config = createConfig();

    config.setEnableStartTls(true);

    LDAPConnection connection = new LDAPConnection(config, sslContext, BIND_DN,
                                  BIND_PWD);

    connection.close();
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws InterruptedException
   * @throws NamingException
   */
  @Test(expected = AuthenticationException.class)
  public void testWithWrongPassword() throws NamingException, IOException
  {
    LDAPConfig config = createConfig();

    config.setEnableStartTls(true);

    LDAPConnection connection = new LDAPConnection(config, sslContext, BIND_DN,
                                  "test-123");

    connection.close();
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws Exception
   */
  private SSLContext createSSLContext() throws Exception
  {
    InputStream input = null;
    OutputStream ouput = null;

    File keystore = tempFolder.newFile("keystore.jks");

    try
    {
      input =
        LdapConnectionTest.class.getResourceAsStream("/security/keystore.jks");
      ouput = new FileOutputStream(keystore);

      IOUtil.copy(input, ouput);
    }
    finally
    {
      IOUtil.close(input);
      IOUtil.close(ouput);
    }

    SSLUtil sslUtil =
      new SSLUtil(new KeyStoreKeyManager(keystore,
        "scm-manager.org".toCharArray()), new TrustAllTrustManager());

    return sslUtil.createSSLContext();
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/07/02
   * @author         Enter your name here...
   */
  private static class AccessLogHandler extends Handler
  {

    /**
     * Method description
     *
     *
     * @throws SecurityException
     */
    @Override
    public void close() throws SecurityException {}

    /**
     * Method description
     *
     */
    @Override
    public void flush() {}

    /**
     * Method description
     *
     *
     * @param record
     */
    @Override
    public void publish(LogRecord record)
    {
      String msg = record.getMessage();
      int index = msg.indexOf("] ");

      if (index > 0)
      {
        msg = msg.substring(index + 2);
      }

      System.out.println(msg);
    }
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  /** Field description */
  private Handler accesslogHandler;

  /** Field description */
  private InMemoryDirectoryServer ds;

  /** Field description */
  private SSLContext sslContext;
}
