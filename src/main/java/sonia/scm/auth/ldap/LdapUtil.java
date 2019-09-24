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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.StartTlsResponse;

/**
 * @author Sebastian Sdorra
 */
class LdapUtil {

  private static final String SCOPE_OBJECT = "object";
  private static final String SCOPE_ONE = "one";
  private static final String SCOPE_SUB = "sub";

  private static final Logger logger = LoggerFactory.getLogger(LdapUtil.class);

  //~--- methods --------------------------------------------------------------


  private LdapUtil() {
  }

  /**
   * Sanitize LDAP search filter to prevent LDAP injection.
   * Source: https://www.owasp.org/index.php/Preventing_LDAP_Injection_in_Java
   * \\\\ is there because java.MessageFormat.format() is suppressing \\ to \
   *
   * @param filter
   * @return
   */
  static String escapeSearchFilter(String filter) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < filter.length(); i++) {
      char curChar = filter.charAt(i);

      switch (curChar) {
        case '\\':
          sb.append("\\5c");

          break;

        case '*':
          sb.append("\\2a");

          break;

        case '(':
          sb.append("\\28");

          break;

        case ')':
          sb.append("\\29");

          break;

        case '\u0000':
          sb.append("\\00");

          break;

        default:
          sb.append(curChar);
      }
    }

    return sb.toString();
  }

  /**
   * Method description
   *
   * @param context
   */
  public static void close(Context context) {
    if (context != null) {
      try {
        context.close();
      } catch (NamingException ex) {
        logger.error("could not close context", ex);
      }
    }
  }

  /**
   * Method description
   *
   * @param tls
   */
  public static void close(StartTlsResponse tls) {
    if (tls != null) {
      try {
        tls.close();
      } catch (IOException ex) {
        logger.error("could not close tls response", ex);
      }
    }
  }

  /**
   * Method description
   *
   * @param enm
   */
  public static void close(NamingEnumeration<?> enm) {
    if (enm != null) {
      try {
        enm.close();
      } catch (NamingException ex) {
        logger.error("could not close enumeration", ex);
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   * @param attributes
   * @param name
   * @return
   */
  public static String getAttribute(Attributes attributes, String name) {
    String value = null;

    try {
      if (Util.isNotEmpty(name)) {
        Attribute attribute = attributes.get(name);

        if (attribute != null) {
          value = (String) attribute.get();
        } else {
          logger.debug("could not find attribute {}", name);
        }
      }
    } catch (NamingException ex) {
      logger.warn("could not fetch attribute ".concat(name), ex);
    }

    return value;
  }

  /**
   * Method description
   *
   * @param dn
   * @return
   */
  public static String getName(String dn) {
    String name = dn;
    int start = dn.indexOf('=');

    if (start > 0) {
      start++;

      int end = dn.indexOf(',');

      if (end > 0) {
        name = dn.substring(start, end);
      } else {
        name = dn.substring(start);
      }
    }

    return name;
  }

  /**
   * Method description
   *
   * @param scopeString
   * @return
   */
  public static int getSearchScope(String scopeString) {
    int scope = SearchControls.SUBTREE_SCOPE;

    if (Util.isNotEmpty(scopeString)) {
      scopeString = scopeString.trim();

      if (SCOPE_SUB.equalsIgnoreCase(scopeString)) {
        scope = SearchControls.SUBTREE_SCOPE;
      } else if (SCOPE_ONE.equalsIgnoreCase(scopeString)) {
        scope = SearchControls.ONELEVEL_SCOPE;
      } else if (SCOPE_OBJECT.equalsIgnoreCase(scopeString)) {
        scope = SearchControls.OBJECT_SCOPE;
      } else if (logger.isWarnEnabled()) {
        logger.warn("unknown scope {}, using subtree scope", scopeString);
      }
    } else if (logger.isWarnEnabled()) {
      logger.warn("no search scope defined, using subtree scope");
    }

    return scope;
  }

  /**
   * Method description
   *
   * @param scope
   * @return
   */
  public static String getSearchScope(int scope) {
    String scopeString = SCOPE_SUB;

    if (scope == SearchControls.ONELEVEL_SCOPE) {
      scopeString = SCOPE_ONE;
    } else if (scope == SearchControls.OBJECT_SCOPE) {
      scopeString = SCOPE_OBJECT;
    }

    return scopeString;
  }

  static String createDN(LdapConfig config, String prefix) {
    if (Util.isNotEmpty(config.getBaseDn())) {
      if (Util.isNotEmpty(prefix)) {
        return prefix.concat(",").concat(config.getBaseDn());
      } else {
        return config.getBaseDn();
      }
    } else {
      throw new ConfigurationException("base dn was not configured");
    }
  }
}