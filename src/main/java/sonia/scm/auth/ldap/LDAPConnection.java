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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.IOException;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

import javax.net.ssl.SSLContext;

/**
 *
 * @author Sebastian Sdorra
 */
public class LDAPConnection implements Closeable
{

  /**
   * the logger for LDAPConnection
   */
  private static final Logger logger =
    LoggerFactory.getLogger(LDAPConnection.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param config
   *
   * @throws IOException
   * @throws NamingException
   */
  public LDAPConnection(LDAPConfig config) throws NamingException, IOException
  {
    this(config, null, null, null);
  }

  /**
   * Constructs ...
   *
   *
   * @param config
   * @param userDN
   * @param password
   *
   * @throws IOException
   * @throws NamingException
   */
  public LDAPConnection(LDAPConfig config, String userDN, String password)
    throws NamingException, IOException
  {
    this(config, null, userDN, password);
  }

  /**
   * Constructs ...
   *
   *
   * @param config
   * @param sslContext
   * @param userDN
   * @param password
   *
   * @throws IOException
   * @throws NamingException
   */
  public LDAPConnection(LDAPConfig config, SSLContext sslContext,
    String userDN, String password)
    throws NamingException, IOException
  {
    context = new InitialLdapContext(createBasicProperties(config, userDN,
      password), null);

    if (config.isEnableStartTls())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("send starttls request");
      }

      tls = (StartTlsResponse) context.extendedOperation(new StartTlsRequest());

      if (sslContext != null)
      {
        tls.negotiate(sslContext.getSocketFactory());
      }
      else
      {
        tls.negotiate();
      }

      // authenticate after bind
      if (userDN != null)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("set bind credentials for dn {}", userDN);
        }

        context.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
        context.addToEnvironment(Context.SECURITY_PRINCIPAL, userDN);

        if (password != null)
        {
          context.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
        }

        // force bind
        context.getAttributes(config.getBaseDn(), new String[] { "dn" });
      }
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    LDAPUtil.close(tls);
    LDAPUtil.close(context);
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param filter
   * @param cons
   *
   * @return
   *
   * @throws NamingException
   */
  public NamingEnumeration<SearchResult> search(String name, String filter,
    SearchControls cons)
    throws NamingException
  {
    return context.search(name, filter, cons);
  }

  /**
   * Method description
   *
   *
   * @param config
   * @param userDN
   * @param password
   *
   * @return
   */
  private Hashtable<String, String> createBasicProperties(LDAPConfig config,
    String userDN, String password)
  {
    Hashtable<String, String> ldapProperties = new Hashtable<String,
                                                 String>(11);

    ldapProperties.put(Context.INITIAL_CONTEXT_FACTORY,
      "com.sun.jndi.ldap.LdapCtxFactory");
    ldapProperties.put(Context.PROVIDER_URL, config.getHostUrl());

    if (Util.isNotEmpty(userDN) && Util.isNotEmpty(password)
      &&!config.isEnableStartTls())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("create context for dn {}", userDN);
      }

      ldapProperties.put(Context.SECURITY_AUTHENTICATION, "simple");
      ldapProperties.put(Context.SECURITY_PRINCIPAL, userDN);
      ldapProperties.put(Context.SECURITY_CREDENTIALS, password);
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("create anonymous context");
    }

    String referral = config.getReferralStrategy().getContextValue();

    logger.debug("use {} as referral strategy", referral);

    ldapProperties.put(Context.REFERRAL, referral);
    ldapProperties.put("java.naming.ldap.version", "3");

    return ldapProperties;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private LdapContext context = null;

  /** Field description */
  private StartTlsResponse tls;
}
