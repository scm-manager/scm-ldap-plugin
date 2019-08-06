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
