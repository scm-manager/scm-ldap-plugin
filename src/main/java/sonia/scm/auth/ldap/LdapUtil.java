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
        logger.warn("could not close context", ex);
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
        logger.warn("could not close tls response", ex);
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
        logger.warn("could not close enumeration", ex);
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
      logger.warn("could not fetch attribute '{}'", name, ex);
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

  public static String getParentDN(String dn) {
    int index = dn.indexOf(',');
    if (index > 0) {
      return dn.substring(index + 1);
    }
    return dn;
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
        logger.info("unknown scope {}, using subtree scope", scopeString);
      }
    } else if (logger.isWarnEnabled()) {
      logger.info("no search scope defined, using subtree scope");
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
