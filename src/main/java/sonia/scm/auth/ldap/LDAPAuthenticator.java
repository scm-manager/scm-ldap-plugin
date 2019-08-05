package sonia.scm.auth.ldap;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.user.User;

import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import java.util.Optional;

class LDAPAuthenticator {

  private final LDAPConfig config;
  private static final Logger logger = LoggerFactory.getLogger(LDAPAuthenticator.class);

  LDAPAuthenticator(LDAPConfig config) {
    this.config = config;
  }

  Optional<User> authenticate(String username, String password) {
    try (LDAPConnection bindConnection = LDAPConnection.createBindConnection(config)) {
      UserSearcher userSearcher = new UserSearcher(config, bindConnection);
      Optional<SearchResult> optionalSearchResult = searchUser(username, userSearcher);
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

  private Optional<SearchResult> searchUser(String username, UserSearcher userSearcher) {
    String nameAttribute = config.getAttributeNameId();
    if (Strings.isNullOrEmpty(nameAttribute)) {
      throw new ConfigurationException("no name attribute was specified");
    }
    return userSearcher.search(username, nameAttribute, config.getAttributeNameFullname(), config.getAttributeNameMail());
  }


  private void authenticateUser(String userDN, String password) {
    try (LDAPConnection connection = LDAPConnection.createUserConnection(config, userDN, password)) {
      logger.debug("user {} successfully authenticated", userDN);
    }
  }

  private User createUser(Attributes attributes) {
    User user = new User();

    user.setType(LdapRealm.TYPE);

    String username = LDAPUtil.getAttribute(attributes, config.getAttributeNameId());
    user.setName(username);
    String displayName = LDAPUtil.getAttribute(attributes, config.getAttributeNameFullname());
    if (Strings.isNullOrEmpty(displayName)) {
      displayName = username;
    }
    user.setDisplayName(displayName);
    user.setMail(LDAPUtil.getAttribute(attributes, config.getAttributeNameMail()));

    if (!user.isValid()) {
      throw new InvalidUserException("invalid user object: " + user.toString(), user);
    }

    return user;
  }
}
