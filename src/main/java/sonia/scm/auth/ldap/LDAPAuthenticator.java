package sonia.scm.auth.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.user.User;
import sonia.scm.util.Util;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class LDAPAuthenticator {

  private final LDAPConfig config;
  private static final Logger logger = LoggerFactory.getLogger(LDAPAuthenticator.class);

  LDAPAuthenticator(LDAPConfig config) {
    this.config = config;
  }

  Optional<User> authenticate(String username, String password) {
    try (LDAPConnection bindConnection = LDAPConnection.createBindConnection(config)) {

      Optional<SearchResult> optionalSearchResult = getUserSearchResult(bindConnection, username);
      if (optionalSearchResult.isPresent()) {
        SearchResult searchResult = optionalSearchResult.get();
        String userDN = searchResult.getNameInNamespace();

        authenticateUser(userDN, password);
        Attributes attributes = searchResult.getAttributes();
        User user = createUser(attributes);

        logger.trace("successfully created user from from ldap response: {}", user);
        return Optional.of(user);
      }
    }

    return Optional.empty();
  }

  private Optional<SearchResult> getUserSearchResult(LDAPConnection bindConnection, String username) {
    SearchControls searchControls = new SearchControls();
    int scope = LDAPUtil.getSearchScope(config.getSearchScope());

    logger.debug("using scope {} for user search", LDAPUtil.getSearchScope(scope));

    searchControls.setSearchScope(scope);
    searchControls.setCountLimit(1);
    searchControls.setReturningAttributes(getReturnAttributes());

    String filter = createUserSearchFilter(username);
    String baseDn = createUserSearchBaseDN();

    try (AutoCloseableNamingEnumeration<SearchResult> searchResultEnm = bindConnection.search(baseDn, filter, searchControls)) {
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

  private String[] getReturnAttributes() {
    List<String> list = new ArrayList<>();

    appendAttribute(list, config.getAttributeNameId());
    appendAttribute(list, config.getAttributeNameFullname());
    appendAttribute(list, config.getAttributeNameMail());
    appendAttribute(list, config.getAttributeNameGroup());

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
        config.getSearchFilter(), LDAPUtil.escapeSearchFilter(username)
      );
      logger.debug("search-filter for user search: {}", filter);
      return filter;
    } else {
      throw new ConfigurationException("search filter not defined");
    }
  }

  private String createUserSearchBaseDN() {
    if (Util.isNotEmpty(config.getBaseDn())) {
      String prefix = config.getUnitPeople();
      if (Util.isNotEmpty(prefix)) {
        return prefix.concat(",").concat(config.getBaseDn());
      } else {
        return config.getBaseDn();
      }
    } else {
      throw new ConfigurationException("base dn was not configured");
    }
  }

  private void authenticateUser(String userDN, String password) {
    try (LDAPConnection connection = LDAPConnection.createUserConnection(config, userDN, password)) {
      logger.debug("user {} successfully authenticated", userDN);
    }
  }

  private User createUser(Attributes attributes) {
    User user = new User();

    user.setType(LDAPAuthenticationHandler.TYPE);
    user.setName(LDAPUtil.getAttribute(attributes, config.getAttributeNameId()));
    user.setDisplayName(LDAPUtil.getAttribute(attributes, config.getAttributeNameFullname()));
    user.setMail(LDAPUtil.getAttribute(attributes, config.getAttributeNameMail()));

    if (!user.isValid()) {
      throw new InvalidUserException("invalid user object: " + user.toString());
    }

    return user;
  }
}
