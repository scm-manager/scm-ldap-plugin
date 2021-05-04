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

import com.google.common.base.Strings;
import com.google.inject.util.Providers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.group.GroupResolver;
import sonia.scm.plugin.Extension;
import sonia.scm.util.Util;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static sonia.scm.auth.ldap.LdapUtil.*;

@Extension
public class LdapGroupResolver implements GroupResolver {

  private static final Logger LOG = LoggerFactory.getLogger(LdapGroupResolver.class);

  private static final String ATTRIBUTE_GROUP_NAME = "cn";
  private static final String NESTEDGROUP_MATCHINGRULE = ":1.2.840.113556.1.4.1941:=";

  private final Provider<LdapConfig> store;

  @Inject
  public LdapGroupResolver(LdapConfigStore store) {
    this((Provider<LdapConfig>) store);
  }

  private LdapGroupResolver(Provider<LdapConfig> store) {
    this.store = store;
  }

  public static LdapGroupResolver from(LdapConfig config) {
    return new LdapGroupResolver(Providers.of(config));
  }

  @Override
  public Set<String> resolve(String principal) {
    LdapConfig config = store.get();
    if (config.isEnabled()) {
      try {
        return sanitizeGroupNames(resolveGroups(config, principal), config);
      } catch (LdapException ex) {
        LOG.error("failed to resolve groups for principal: {}", ex);
      }
    } else {
      LOG.debug("ldap is disabled, returning empty set of groups");
    }
    return Collections.emptySet();
  }

  private Set<String> sanitizeGroupNames(Set<String> groups, LdapConfig config) {
    if (config.isRemoveInvalidCharacters()) {
      return groups.stream().map(name -> name.replaceAll("[:/?#;&=\\s%\\\\]", "_")).collect(toSet());
    } else {
      return groups;
    }
  }

  private Set<String> resolveGroups(LdapConfig config, String principal) {
    try (LdapConnection bindConnection = LdapConnection.createBindConnection(config)) {
      UserSearcher searcher = new UserSearcher(config, bindConnection);
      Optional<SearchResult> optionalSearchResult = searcher.search(principal, config.getAttributeNameGroup(), config.getAttributeNameMail());
      if (optionalSearchResult.isPresent()) {
        SearchResult searchResult = optionalSearchResult.get();

        Attributes attributes = searchResult.getAttributes();
        String userDn = searchResult.getNameInNamespace();
        String mailAttribute = getAttribute(attributes, config.getAttributeNameMail());
        Set<String> groups = new HashSet<>();
        groups.addAll(fetchGroups(bindConnection, userDn, principal, mailAttribute));
        groups.addAll(getGroups(attributes));
        if(config.isEnableNestedGroups()){
          groups = computeRecursiveGroups(bindConnection, groups);
        }
        return groups.stream().map(dn -> {return LdapUtil.getName(dn);}).collect(Collectors.toSet());
      }
    }
    return Collections.emptySet();
  }

  private Set<String> computeRecursiveGroups(LdapConnection connection, Set<String> groups) {
    //queue for yet to recursively searched groups
    Queue<String> toSearch = new LinkedList<>(groups);
    //the result set
    Set<String> found = new HashSet<>(groups);

    LOG.trace("fetching recursive defined groups");

    //loop until fixpoint is reached
    while(!toSearch.isEmpty()) {
      String groupDN = toSearch.poll();

      Optional<String> nestedFilter = createNestedGroupSearchFilter(groupDN);
      Set<String> currentGroups = new HashSet<>();
      if(nestedFilter.isPresent()) {
        currentGroups = fetchGroupByFilter(connection, nestedFilter.get());
      }
      for (String group : currentGroups) {
        if (!found.contains(group)) {
          toSearch.add(group);
          found.add(group);
        }
      }
    }
    return found;
  }

  private Set<String> getGroups(Attributes attributes) {
    Set<String> groups = new HashSet<>();

    LdapConfig config = store.get();
    String groupAttribute = config.getAttributeNameGroup();

    if (Util.isNotEmpty(groupAttribute)) {
      LOG.trace("try to get groups from group attribute {}", groupAttribute);
      NamingEnumeration<?> userGroupsEnm = null;

      try {
        Attribute groupsAttribute = attributes.get(groupAttribute);
        if (groupsAttribute != null) {
          userGroupsEnm = groupsAttribute.getAll();

          while (userGroupsEnm.hasMore()) {
            String group = (String) userGroupsEnm.next();

            LOG.debug("append group {} to user result", group);
            groups.add(group);
          }
        } else {
          LOG.debug("user has no group attributes assigned");
        }
      } catch (NamingException ex) {
        LOG.warn("could not read group attribute", ex);
      } finally {
        close(userGroupsEnm);
      }
    } else {
      LOG.debug("group attribute is empty");
    }
    return groups;
  }

  private Set<String> fetchGroups(LdapConnection connection, String userDN, String uid, String mail) {
    Set<String> groups = new HashSet<>();

    Optional<String> optionalFilter = createGroupSearchFilter(userDN, uid, mail);
    if (optionalFilter.isPresent()) {
      LOG.trace("try to fetch groups for user {}", uid);
      groups = fetchGroupByFilter(connection, optionalFilter.get());
    } else {
      LOG.debug("group filter is empty");
    }
    return groups;
  }

  private Set<String> fetchGroupByFilter(LdapConnection connection, String filter){
    Set<SearchResult> results = searchGroup(connection, filter);
    Set<String> groups = new HashSet<>();
    for(SearchResult searchResult: results){
      String dn = searchResult.getNameInNamespace();
      LOG.trace("append group {} to user result", dn);
      groups.add(dn);
    }
    return groups;
  }

  private Set<SearchResult> searchGroup(LdapConnection connection, String filter){
    Set<SearchResult> results = new HashSet<>();

    // read group of unique names
    SearchControls searchControls = new SearchControls();

    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

    // make group name attribute configurable?
    searchControls.setReturningAttributes(new String[]{ATTRIBUTE_GROUP_NAME});

    LdapConfig config = store.get();
    String searchDN = LdapUtil.createDN(config, config.getUnitGroup());
    LOG.debug("search groups at {} with filter {}", searchDN, filter);

    try (AutoCloseableNamingEnumeration<SearchResult> searchResultEnm = connection.search(searchDN, filter, searchControls)) {
      while (searchResultEnm.hasMore()) {
        SearchResult searchResult = searchResultEnm.next();
        results.add(searchResult);
        LOG.trace("append group {} to result", searchResult.getNameInNamespace());
      }
    } catch (NamingException ex) {
      LOG.debug("could not find groups", ex);
    }
    return results;
  }

  private Optional<String> createGroupSearchFilter(String userDN, String uid, String mail) {
    LdapConfig config = store.get();
    String filterPattern = config.getSearchFilterGroup();

    if (Util.isNotEmpty(filterPattern)) {

      if (config.isEnableNestedADGroups()) {
        filterPattern = prepareFilterPatternForADNestedGroups(filterPattern,
          escapeLDAPSearchFilter(userDN));
      }
      String filter = MessageFormat.format(filterPattern, escapeLDAPSearchFilter(userDN), uid, Strings.nullToEmpty(mail));
      LOG.debug("search-filter for group search: {}", filter);

      return Optional.of(filter);
    } else {
      LOG.debug("search-filter for groups not defined");
    }
    return Optional.empty();
  }

  private String prepareFilterPatternForADNestedGroups(String filterPattern, String userDN) {
    return filterPattern.replaceAll(Pattern.quote("={0}"),
      NESTEDGROUP_MATCHINGRULE.concat(userDN));
  }

  private Optional<String> createNestedGroupSearchFilter(String groupDN){
    LdapConfig config = store.get();
    String filterPattern = config.getSearchFilterNestedGroup();
    String groupCN = LdapUtil.getName(groupDN);

    if (Util.isNotEmpty(filterPattern)) {
      String filter = MessageFormat.format(filterPattern, escapeLDAPSearchFilter(groupDN),escapeLDAPSearchFilter(groupCN));
      LOG.debug("search-filter for group search: {}", filter);
      return Optional.of(filter);
    } else {
      LOG.debug("search-filter for groups not defined");
    }
    return Optional.empty();
  }

  private Optional<String> createGroupSearchFilterByName(String groupCN) {
    LdapConfig config = store.get();
    String filterPattern = ATTRIBUTE_GROUP_NAME+"={0}";

    String filter = MessageFormat.format(filterPattern, escapeLDAPSearchFilter(groupCN));
    LOG.debug("search-filter for group search: {}", filter);

    return Optional.of(filter);
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
    StringBuilder sb = new StringBuilder();
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
}
