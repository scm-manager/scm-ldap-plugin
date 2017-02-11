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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;
import sonia.scm.web.security.AuthenticationResult;
import sonia.scm.web.security.AuthenticationState;

//~--- JDK imports ------------------------------------------------------------

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 *
 * @author Sebastian Sdorra
 */
public class LDAPAuthenticationContext
{

  /** Field description */
  public static final String ATTRIBUTE_GROUP_NAME = "cn";

  /** Field description */
  public static final String NESTEDGROUP_MATCHINGRULE =
    ":1.2.840.113556.1.4.1941:=";

  /** Field description */
  public static final String SEARCHTYPE_GROUP = "group";

  /** Field description */
  public static final String SEARCHTYPE_USER = "user";

  /** the logger for LDAPContext */
  private static final Logger logger =
    LoggerFactory.getLogger(LDAPAuthenticationContext.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param config
   */
  public LDAPAuthenticationContext(LDAPConfigList config)
  {
    this.config = config;
    this.state = new LDAPAuthenticationState();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param username
   * @param password
   *
   * @return
   */
  public AuthenticationResult authenticate(String username, String password)
  {
      AuthenticationResult result = AuthenticationResult.NOT_FOUND;
      AssertUtil.assertIsNotEmpty(username);
      AssertUtil.assertIsNotEmpty(password);

      List<LDAPConfig> configs = config.getLDAPConfigList();

      // If we have a forced domain in the username field then we should use that.
      if (username.indexOf("\\") != -1) {
          String[] items = username.split("\\\\");
          String forcedDomain = items[0];
          for(LDAPConfig config: configs) {
            if (config.isEnabled() && config.getUniqueId().equals(forcedDomain)) {
              result = authenticate(config, items[1], password);
              return result;
            }
          }
      }

    for(LDAPConfig subConfig: configs) {
          if (subConfig.isEnabled()) {
              result = authenticate(subConfig, username, password);

              // If we have not found the user in this config then we
              // should try the remaining configurations. Else we
              // should just return the result.
              if (result.getState() == AuthenticationState.NOT_FOUND) {
                  continue;
              } else {
                  break;
              }
          }

      }

      return result;
  }

  /**
   * Method description
   *
   *
   * @param username
   * @param password
   * @param config
   *
   * @return
   */
  public AuthenticationResult authenticate(LDAPConfig config, String username, String password)
  {
    AuthenticationResult result = AuthenticationResult.NOT_FOUND;
    LDAPConnection bindConnection = null;

    try
    {
      bindConnection = createBindConnection(config);

      if (bindConnection != null)
      {
        SearchResult searchResult = getUserSearchResult(
          config,
          bindConnection,
          username
        );

        if (searchResult != null)
        {
          result = AuthenticationResult.FAILED;

          String userDN = searchResult.getNameInNamespace();

          if (authenticateUser(config, userDN, password))
          {
            Attributes attributes = searchResult.getAttributes();
            User user = createUser(config, attributes);

            if (user.isValid())
            {
              logger.trace(
                "succefully created user from from ldap response: {}", user);
              state.setUserValid(true);

              Set<String> groups = new HashSet<String>();

              fetchGroups(config, bindConnection, groups, userDN, user.getId(),
                user.getMail());
              getGroups(config, attributes, groups);
              result = new AuthenticationResult(user, groups);
            }
            else if (logger.isWarnEnabled())
            {
              logger.warn("the returned user is not valid: {}", user);
            }
          }    // password wrong ?
        }      // user not found
      }        // no bind context available
    }
    finally
    {
      IOUtil.close(bindConnection);
    }

    logger.trace("return authentication result: {}", result);

    return result;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public LDAPAuthenticationState getState()
  {
    return state;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param list
   * @param attribute
   */
  private void appendAttribute(List<String> list, String attribute)
  {
    if (Util.isNotEmpty(attribute))
    {
      list.add(attribute);
    }
  }

  /**
   * Method description
   *
   *
   * @param userDN
   * @param password
   * @param config
   *
   * @return
   */
  private boolean authenticateUser(LDAPConfig config, String userDN, String password)
  {
    boolean authenticated = false;
    LDAPConnection userConnection = null;

    try
    {
      userConnection = new LDAPConnection(config, userDN, password);
      authenticated = true;
      state.setAuthenticateUser(true);

      if (logger.isDebugEnabled())
      {
        logger.debug("user {} successfully authenticated", userDN);
      }
    }
    catch (Exception ex)
    {
      state.setAuthenticateUser(false);
      state.setException(ex);

      if (logger.isTraceEnabled())
      {
        logger.trace("authentication failed for user ".concat(userDN), ex);
      }
      else if (logger.isWarnEnabled())
      {
        logger.debug("authentication failed for user {}", userDN);
      }
    }
    finally
    {
      IOUtil.close(userConnection);
    }

    return authenticated;
  }

  /**
   * Method description
   *
   * @param config
   * @return
   */
  private LDAPConnection createBindConnection(LDAPConfig config)
  {
    LDAPConnection connection = null;

    try
    {
      connection = new LDAPConnection(
        config,
        config.getConnectionDn(),
        config.getConnectionPassword()
      );
      state.setBind(true);
    }
    catch (Exception ex)
    {
      state.setBind(false);
      state.setException(ex);
      logger.error(
        "could not bind to ldap with dn ".concat(config.getConnectionDn()), ex);
      IOUtil.close(connection);
    }

    return connection;
  }

  /**
   * Method description
   *
   * @param config
   * @return
   */
  private String createGroupSearchBaseDN(LDAPConfig config)
  {
    return createSearchBaseDN(config, SEARCHTYPE_GROUP, config.getUnitGroup());
  }

  /**
   * Method description
   *
   *
   * @param userDN
   * @param uid
   * @param mail
   * @param config
   *
   * @return
   */
  private String createGroupSearchFilter(LDAPConfig config, String userDN, String uid, String mail)
  {
    String filter = null;
    String filterPattern = config.getSearchFilterGroup();

    if (Util.isNotEmpty(filterPattern))
    {
      if (mail == null)
      {
        mail = "";
      }

      if (config.isEnableNestedADGroups())
      {
        filterPattern = prepareFilterPatternForNestedGroups(filterPattern,
         escapeLDAPSearchFilter(userDN));
      }

      filter = MessageFormat.format(filterPattern, escapeLDAPSearchFilter(userDN), uid, mail);

      if (logger.isDebugEnabled())
      {
        logger.debug("search-filter for group search: {}", filter);
      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("search-filter for groups not defined");
    }

    return filter;
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param prefix
   * @param config
   *
   * @return
   */
  private String createSearchBaseDN(LDAPConfig config, String type, String prefix)
  {
    String dn = null;

    if (Util.isNotEmpty(config.getBaseDn()))
    {
      if (Util.isNotEmpty(prefix))
      {
        dn = prefix.concat(",").concat(config.getBaseDn());
      }
      else
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("no prefix for {} defined, using basedn for search",
            type);
        }

        dn = config.getBaseDn();
      }

      if (logger.isDebugEnabled())
      {
        logger.debug("search base for {} search: {}", type, dn);
      }
    }
    else if (logger.isErrorEnabled())
    {
      logger.error("no basedn defined");
    }

    return dn;
  }

  /**
   * Method description
   *
   *
   * @param attributes
   * @param config
   *
   * @return
   */
  private User createUser(LDAPConfig config, Attributes attributes)
  {
    User user = new User();

    user.setName(LDAPUtil.getAttribute(attributes,
      config.getAttributeNameId()));
    user.setDisplayName(LDAPUtil.getAttribute(attributes,
      config.getAttributeNameFullname()));
    user.setMail(LDAPUtil.getAttribute(attributes,
      config.getAttributeNameMail()));
    user.setType(LDAPAuthenticationHandler.TYPE);

    return user;
  }

  /**
   * Method description
   *
   * @param config
   * @return
   */
  private String createUserSearchBaseDN(LDAPConfig config)
  {
    return createSearchBaseDN(config, SEARCHTYPE_USER, config.getUnitPeople());
  }

  /**
   * Method description
   *
   * @param config
   * @param username
   *
   * @return
   */
  private String createUserSearchFilter(LDAPConfig config, String username)
  {
    String filter = null;

    if (Util.isNotEmpty(config.getSearchFilter()))
    {
      filter = MessageFormat.format(
        config.getSearchFilter(), LDAPUtil.escapeSearchFilter(username)
      );

      if (logger.isDebugEnabled())
      {
        logger.debug("search-filter for user search: {}", filter);
      }
    }
    else if (logger.isErrorEnabled())
    {
      logger.error("search filter not defined");
    }

    return filter;
  }

  /**
   * Method description
   *
   * @param config
   * @param connection
   * @param groups
   * @param userDN
   * @param uid
   * @param mail
   */
  private void fetchGroups(LDAPConfig config, LDAPConnection connection, Set<String> groups,
                           String userDN, String uid, String mail)
  {
    if (Util.isNotEmpty(config.getSearchFilterGroup()))
    {
      logger.trace("try to fetch groups for user {}", uid);

      NamingEnumeration<SearchResult> searchResultEnm = null;

      try
      {

        // read group of unique names
        SearchControls searchControls = new SearchControls();

        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        // make group name attribute configurable?
        searchControls.setReturningAttributes(new String[] {
          ATTRIBUTE_GROUP_NAME });

        String filter = createGroupSearchFilter(config, userDN, uid, mail);

        if (filter != null)
        {
          String searchDN = createGroupSearchBaseDN(config);

          if (logger.isDebugEnabled())
          {
            logger.debug("search groups for user {} at {} with filter {}",
              new Object[] { userDN,
              searchDN, filter });
          }

          searchResultEnm = connection.search(searchDN, filter, searchControls);

          while (searchResultEnm.hasMore())
          {
            SearchResult searchResult = searchResultEnm.next();
            Attributes groupAttributes = searchResult.getAttributes();
            String name = LDAPUtil.getAttribute(groupAttributes,
                            ATTRIBUTE_GROUP_NAME);

            if (Util.isNotEmpty(name))
            {
              if (logger.isTraceEnabled())
              {
                logger.trace("append group {} with name {} to user result",
                  searchResult.getNameInNamespace(), name);
              }

              groups.add(name);
            }
            else if (logger.isDebugEnabled())
            {
              logger.debug("could not read group name from {}",
                searchResult.getNameInNamespace());
            }
          }
        }
      }
      catch (NamingException ex)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("could not find groups", ex);
        }
      }
      finally
      {
        LDAPUtil.close(searchResultEnm);
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("group filter is empty");
    }
  }

  /**
   * Method description
   *
   *
   * @param filterPattern
   * @param userDN
   *
   * @return
   */
  private String prepareFilterPatternForNestedGroups(String filterPattern,
    String userDN)
  {
    return filterPattern.replaceAll(Pattern.quote("={0}"),
      NESTEDGROUP_MATCHINGRULE.concat(userDN));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   * @param config
   * @param attributes
   * @param groups
   *
   */
  private void getGroups(LDAPConfig config, Attributes attributes, Set<String> groups)
  {
    String groupAttribute = config.getAttributeNameGroup();

    if (Util.isNotEmpty(groupAttribute))
    {
      logger.trace("try to get groups from group attribute {}", groupAttribute);

      NamingEnumeration<?> userGroupsEnm = null;

      try
      {
        Attribute groupsAttribute = attributes.get(groupAttribute);

        if (groupsAttribute != null)
        {
          userGroupsEnm = groupsAttribute.getAll();

          while (userGroupsEnm.hasMore())
          {
            String group = (String) userGroupsEnm.next();

            group = LDAPUtil.getName(group);
            logger.debug("append group {} to user result", group);
            groups.add(group);
          }
        }
        else if (logger.isDebugEnabled())
        {
          logger.debug("user has no group attributes assigned");
        }
      }
      catch (NamingException ex)
      {
        logger.error("could not read group attribute", ex);
      }
      finally
      {
        LDAPUtil.close(userGroupsEnm);
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("group attribute is empty");
    }
  }

  /**
   * Method description
   *
   * @param config
   * @return
   */
  private String[] getReturnAttributes(LDAPConfig config)
  {
    List<String> list = new ArrayList<String>();

    appendAttribute(list, config.getAttributeNameId());
    appendAttribute(list, config.getAttributeNameFullname());
    appendAttribute(list, config.getAttributeNameMail());
    appendAttribute(list, config.getAttributeNameGroup());

    return list.toArray(new String[list.size()]);
  }

  /**
   * Method description
   *
   * @param config
   * @param bindConnection
   * @param username
   *
   * @return
   */
  private SearchResult getUserSearchResult(LDAPConfig config, LDAPConnection bindConnection,
                                           String username)
  {
    SearchResult result = null;

    if (bindConnection != null)
    {
      NamingEnumeration<SearchResult> searchResultEnm = null;

      try
      {
        SearchControls searchControls = new SearchControls();
        int scope = LDAPUtil.getSearchScope(config.getSearchScope());

        if (logger.isDebugEnabled())
        {
          logger.debug("using scope {} for user search",
            LDAPUtil.getSearchScope(scope));
        }

        searchControls.setSearchScope(scope);
        searchControls.setCountLimit(1);
        searchControls.setReturningAttributes(getReturnAttributes(config));

        String filter = createUserSearchFilter(config, username);

        if (filter != null)
        {
          String baseDn = createUserSearchBaseDN(config);

          if (baseDn != null)
          {
            searchResultEnm = bindConnection.search(baseDn, filter,
              searchControls);

            if (searchResultEnm.hasMore())
            {
              result = searchResultEnm.next();
              state.setSearchUser(true);
            }
            else if (logger.isWarnEnabled())
            {
              logger.warn("no user with username {} found", username);
            }
          }
        }
      }
      catch (NamingException ex)
      {
        state.setSearchUser(false);
        state.setException(ex);

        if (logger.isErrorEnabled())
        {
          logger.error("exception occured during user search", ex);
        }
      }
      finally
      {
        LDAPUtil.close(searchResultEnm);
      }
    }

    return result;
  }
  
  /**
   * Sanitize LDAP search filter to prevent LDAP injection.
   * Source: https://www.owasp.org/index.php/Preventing_LDAP_Injection_in_Java
   * \\\\ is there because java.MessageFormat.format() is suppressing \\ to \
   * 
   * @param filter
   * @return
   */
  public String escapeLDAPSearchFilter(String filter) {
      StringBuilder sb = new StringBuilder(); // If using JDK >= 1.5 consider using StringBuilder
      for (int i = 0; i < filter.length(); i++) {
          char curChar = filter.charAt(i);
          switch (curChar) {
              case '\\':
                  sb.append("\\\\5c");
                  break;
              case '*':
                  sb.append("\\\\2a");
                  break;
              case '(':
                  sb.append("\\\\28");
                  break;
              case ')':
                  sb.append("\\\\29");
                  break;
              case '\u0000': 
                  sb.append("\\\\00"); 
                  break;
              default:
                  sb.append(curChar);
          }
      }
      return sb.toString();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private LDAPConfigList config;

  /** Field description */
  private LDAPAuthenticationState state;
}
