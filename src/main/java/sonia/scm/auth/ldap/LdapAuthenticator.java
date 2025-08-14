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

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.user.User;
import sonia.scm.util.ValidationUtil;

import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import java.util.Optional;

@Slf4j
public class LdapAuthenticator {

  private final LdapConnectionFactory connectionFactory;
  private final LdapConfig config;

  public LdapAuthenticator(LdapConnectionFactory connectionFactory, LdapConfig config) {
    this.connectionFactory = connectionFactory;
    this.config = config;
  }

  public Optional<User> authenticate(String username, String password) {
    try (LdapConnection bindConnection = connectionFactory.createBindConnection(config)) {
      log.trace("bind connection to ldap server established");
      UserSearcher userSearcher = new UserSearcher(config, bindConnection);
      Optional<SearchResult> optionalSearchResult = searchUser(username, userSearcher);
      if (optionalSearchResult.isPresent()) {
        SearchResult searchResult = optionalSearchResult.get();
        String userDN = searchResult.getNameInNamespace();

        authenticateUser(userDN, password);
        Attributes attributes = searchResult.getAttributes();
        User user = createUser(attributes);
        log.trace("successfully created external user from ldap response: {}", user);
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
    try (LdapConnection ignored = connectionFactory.createUserConnection(config, userDN, password)) {
      log.debug("user {} successfully authenticated", userDN);
    }
  }

  private User createUser(Attributes attributes) {
    User user = new User();
    user.setExternal(true);

    String username = LdapUtil.getAttribute(attributes, config.getAttributeNameId());
    user.setName(username);
    String displayName = LdapUtil.getAttribute(attributes, config.getAttributeNameFullname());
    if (Strings.isNullOrEmpty(displayName)) {
      displayName = username;
    }
    user.setDisplayName(displayName);

    String mail = LdapUtil.getAttribute(attributes, config.getAttributeNameMail());
    if (ValidationUtil.isMailAddressValid(mail)) {
      user.setMail(mail);
    } else {
      log.warn("No valid e-mail address found for user {}", username);
    }

    if (!user.isValid()) {
      throw new InvalidUserException("invalid user object: " + user, user);
    }

    return user;
  }
}
