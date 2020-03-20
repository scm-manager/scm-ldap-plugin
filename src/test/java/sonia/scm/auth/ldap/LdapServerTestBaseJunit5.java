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

public class LdapServerTestBaseJunit5 extends LdapTestBase {

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

    // disable schema check, because of memberOf attribute
    config.setSchema(null);
    ldapServer = new InMemoryDirectoryServer(config);
    ldapServer.startListening();
  }


  protected void ldif(int number) {
    String ldif = String.format("/ldif/%03d.ldif", number);
    try (InputStream stream = LdapServerTestBase.class.getResourceAsStream(ldif)) {
      LDIFReader reader = new LDIFReader(stream);

      ldapServer.importFromLDIF(true, reader);
    } catch (LDAPException | IOException ex) {
      Assertions.fail("failed to load ldif " + ldif, ex);
    }
  }

}
