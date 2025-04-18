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

  private static final String PROPERTY_SSL_SOCKET_FACTORY = "java.naming.ldap.factory.socket";

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
  private final SSLContext sslContext;

  @VisibleForTesting
  LdapConnection(LdapConfig config, SSLContext sslContext, String userDN, String password) throws NamingException, IOException {
    this.sslContext = sslContext;
    ThreadLocalSocketFactory.setDelegate(sslContext.getSocketFactory());
    // JNDI uses the context classloader to instantiate the socket factory
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    Thread.currentThread().setContextClassLoader(LdapConnection.class.getClassLoader());
    try {
      context = new InitialLdapContext(createConnectionProperties(config, userDN, password), null);

      if (config.isEnableStartTls()) {
        startTLS(config, userDN, password);
      }
    } finally {
      Thread.currentThread().setContextClassLoader(classLoader);
    }
  }

  private void startTLS(LdapConfig config, String userDN, String password) throws NamingException, IOException {
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
  private Hashtable<String, Object> createConnectionProperties(LdapConfig config, String userDN, String password) {
    Hashtable<String, Object> ldapProperties = new Hashtable<>(12);

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

    if (config.getHostUrl().startsWith("ldaps")) {
      ldapProperties.put(Context.SECURITY_PROTOCOL, "ssl");
      ldapProperties.put(PROPERTY_SSL_SOCKET_FACTORY, ThreadLocalSocketFactory.class.getName());
    }

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
    ThreadLocalSocketFactory.clearDelegate();
  }
}
