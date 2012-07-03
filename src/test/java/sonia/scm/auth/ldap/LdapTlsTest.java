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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import java.security.KeyStore;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.naming.NamingException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author Sebastian Sdorra
 */
public class LdapTlsTest extends LDAPTestBase
{

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
    config.setSchema(null);

    ds = new InMemoryDirectoryServer(config);
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
  public void testTlsConnection()
    throws NamingException, IOException, InterruptedException
  {
    LDAPConfig config = createConfig();

    config.setEnableStartTls(false);

    LDAPConnection connection = new LDAPConnection(config, BIND_DN, BIND_PWD);

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
    KeyStore keyStore = KeyStore.getInstance("JKS");
    InputStream input = null;

    try
    {
      input = LdapTlsTest.class.getResourceAsStream("security/keystore.jks");
      keyStore.load(input, "scm-manager.org".toCharArray());
    }
    finally
    {
      IOUtil.close(input);
    }

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

    kmf.init(keyStore, "scm-manager.org".toCharArray());

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

    tmf.init(keyStore);

    SSLContext ctx = SSLContext.getInstance("TLSv1");

    ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

    return ctx;
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
