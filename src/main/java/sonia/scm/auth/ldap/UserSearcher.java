package sonia.scm.auth.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.Util;

import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class UserSearcher {

  private static final Logger logger = LoggerFactory.getLogger(UserSearcher.class);

  private final LdapConfig config;
  private final LdapConnection connection;

  UserSearcher(LdapConfig config, LdapConnection connection) {
    this.config = config;
    this.connection = connection;
  }

  Optional<SearchResult> search(String username, String... attributes) {
    SearchControls searchControls = new SearchControls();
    int scope = LdapUtil.getSearchScope(config.getSearchScope());

    logger.debug("using scope {} for user search", LdapUtil.getSearchScope(scope));

    searchControls.setSearchScope(scope);
    searchControls.setCountLimit(1);
    searchControls.setReturningAttributes(getReturnAttributes(attributes));

    String filter = createUserSearchFilter(username);
    String baseDn = LdapUtil.createDN(config, config.getUnitPeople());

    try (AutoCloseableNamingEnumeration<SearchResult> searchResultEnm = connection.search(baseDn, filter, searchControls)) {
      if (searchResultEnm.hasMore()) {
        return Optional.of(searchResultEnm.next());
      } else {
        logger.warn("no user with username {} found", username);
        return Optional.empty();
      }
    } catch (NamingException ex) {
      throw new UserSearchFailedException("exception occurred during user search", ex);
    }
  }

  private String[] getReturnAttributes(String[] attributes) {
    List<String> list = new ArrayList<>();
    for (String attribute : attributes) {
      appendAttribute(list, attribute);
    }
    return list.toArray(new String[0]);
  }

  private void appendAttribute(List<String> list, String attribute) {
    if (Util.isNotEmpty(attribute)) {
      list.add(attribute);
    }
  }

  private String createUserSearchFilter(String username) {
    if (Util.isNotEmpty(config.getSearchFilter())) {
      String filter = MessageFormat.format(
        config.getSearchFilter(), LdapUtil.escapeSearchFilter(username)
      );
      logger.debug("search-filter for user search: {}", filter);
      return filter;
    } else {
      throw new ConfigurationException("search filter not defined");
    }
  }

}
