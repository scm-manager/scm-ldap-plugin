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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.Util;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.SSLContext;
import java.io.Closeable;
import java.io.IOException;
import java.util.Hashtable;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
class LdapConnection implements Closeable {

  /**
   * property for ldap connect timeout
   */
  private static final String PROPERTY_TIMEOUT_CONNECT =
    "com.sun.jndi.ldap.connect.timeout";

  /**
   * property for ldap read timeout
   */
  private static final String PROPERTY_TIMEOUT_READ =
    "com.sun.jndi.ldap.read.timeout";

  /**
   * connect timeout: 5sec
   */
  private static final String TIMEOUT_CONNECT = "5000";

  /**
   * read timeout: 2min
   */
  private static final String TIMEOUT_READ = "120000";

  /**
   * the logger for LDAPConnection
   */
  private static final Logger logger =
    LoggerFactory.getLogger(LdapConnection.class);

  private final LdapContext context;
  private StartTlsResponse tls;

  static LdapConnection createBindConnection(LdapConfig config) {
    try {
      return new LdapConnection(config, null, config.getConnectionDn(), config.getConnectionPassword());
    } catch (IOException | NamingException ex) {
      throw new BindConnectionFailedException("failed to create bind connection for " + config.getConnectionDn(), ex);
    }
  }

  static LdapConnection createUserConnection(LdapConfig config, String userDn, String password) {
    try {
      return new LdapConnection(config, null, userDn, password);
    } catch (IOException | NamingException ex) {
      throw new UserAuthenticationFailedException("failed to authenticate user " + userDn, ex);
    }
  }

  @VisibleForTesting
  LdapConnection(LdapConfig config, SSLContext sslContext, String userDN, String password) throws NamingException, IOException {
    context = new InitialLdapContext(createConnectionProperties(config, userDN, password), null);

    if (config.isEnableStartTls()) {
      startTLS(config, sslContext, userDN, password);
    }
  }

  private void startTLS(LdapConfig config, SSLContext sslContext, String userDN, String password) throws NamingException, IOException {
    logger.debug("send starttls request");

    tls = (StartTlsResponse) context.extendedOperation(new StartTlsRequest());

    if (sslContext != null) {
      tls.negotiate(sslContext.getSocketFactory());
    } else {
      tls.negotiate();
    }

    // authenticate after bind
    if (userDN != null) {
      logger.debug("set bind credentials for dn {}", userDN);

      context.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
      context.addToEnvironment(Context.SECURITY_PRINCIPAL, userDN);

      if (password != null) {
        context.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
      } else {
        logger.debug("try to bind user {} without password", userDN);
      }

      // force bind
      logger.trace("fetch dn of {} to force bind", config.getBaseDn());
      context.getAttributes(config.getBaseDn(), new String[]{"dn"});
    }
  }

  @SuppressWarnings("squid:S1149") // we have to use hashtable, because it is required by jndi
  private Hashtable<String, String> createConnectionProperties(LdapConfig config, String userDN, String password) {
    Hashtable<String, String> ldapProperties = new Hashtable<>(11);

    ldapProperties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    ldapProperties.put(Context.PROVIDER_URL, config.getHostUrl());

    // apply timeout for read and connect
    // see https://groups.google.com/d/topic/scmmanager/QTimDQM2Wfw/discussion
    ldapProperties.put(PROPERTY_TIMEOUT_CONNECT, TIMEOUT_CONNECT);
    ldapProperties.put(PROPERTY_TIMEOUT_READ, TIMEOUT_READ);

    if (Util.isNotEmpty(userDN) && Util.isNotEmpty(password) && !config.isEnableStartTls()) {
      logger.debug("create context for dn {}", userDN);

      ldapProperties.put(Context.SECURITY_AUTHENTICATION, "simple");
      ldapProperties.put(Context.SECURITY_PRINCIPAL, userDN);
      ldapProperties.put(Context.SECURITY_CREDENTIALS, password);
    } else {
      logger.debug("create anonymous context");
    }

    String referral = config.getReferralStrategy().getContextValue();

    logger.debug("use {} as referral strategy", referral);

    ldapProperties.put(Context.REFERRAL, referral);
    ldapProperties.put("java.naming.ldap.version", "3");

    return ldapProperties;
  }

  AutoCloseableNamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
    throws NamingException {
    return new AutoCloseableNamingEnumeration<>(context.search(name, filter, cons));
  }

  @Override
  public void close() {
    LdapUtil.close(tls);
    LdapUtil.close(context);
  }
}
