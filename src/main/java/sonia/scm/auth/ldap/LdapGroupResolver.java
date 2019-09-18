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
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

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
      return resolveGroups(config, principal);
    } else {
      LOG.debug("ldap is disabled, returning empty set of groups");
    }
    return Collections.emptySet();
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
        Set<String> groups = fetchGroups(bindConnection, userDn, principal, mailAttribute);
        groups.addAll(getGroups(attributes));
        return groups;
      }
    }
    return Collections.emptySet();
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

            group = getName(group);
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


      // read group of unique names
      SearchControls searchControls = new SearchControls();

      searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

      // make group name attribute configurable?
      searchControls.setReturningAttributes(new String[]{ATTRIBUTE_GROUP_NAME});

      String filter = optionalFilter.get();

      LdapConfig config = store.get();
      String searchDN = LdapUtil.createDN(config, config.getUnitGroup());
      LOG.debug("search groups for user {} at {} with filter {}", userDN, searchDN, filter);

      try (AutoCloseableNamingEnumeration<SearchResult> searchResultEnm = connection.search(searchDN, filter, searchControls)) {
        while (searchResultEnm.hasMore()) {
          SearchResult searchResult = searchResultEnm.next();
          Attributes groupAttributes = searchResult.getAttributes();
          String name = getAttribute(groupAttributes,
            ATTRIBUTE_GROUP_NAME);

          if (Util.isNotEmpty(name)) {
            LOG.trace("append group {} with name {} to user result", searchResult.getNameInNamespace(), name);
            groups.add(name);
          } else {
            LOG.debug("could not read group name from {}", searchResult.getNameInNamespace());
          }
        }
      } catch (NamingException ex) {
        LOG.debug("could not find groups", ex);
      }
    } else {
      LOG.debug("group filter is empty");
    }
    return groups;
  }

  private Optional<String> createGroupSearchFilter(String userDN, String uid, String mail) {
    LdapConfig config = store.get();
    String filterPattern = config.getSearchFilterGroup();

    if (Util.isNotEmpty(filterPattern)) {

      if (config.isEnableNestedADGroups()) {
        filterPattern = prepareFilterPatternForNestedGroups(filterPattern,
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

  private String prepareFilterPatternForNestedGroups(String filterPattern, String userDN) {
    return filterPattern.replaceAll(Pattern.quote("={0}"),
      NESTEDGROUP_MATCHINGRULE.concat(userDN));
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
