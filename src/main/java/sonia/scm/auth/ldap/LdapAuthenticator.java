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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.user.User;
import sonia.scm.util.ValidationUtil;

import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import java.util.Optional;

public class LdapAuthenticator {

  private final LdapConfig config;
  private static final Logger logger = LoggerFactory.getLogger(LdapAuthenticator.class);

  public LdapAuthenticator(LdapConfig config) {
    this.config = config;
  }

  public Optional<User> authenticate(String username, String password) {
    try (LdapConnection bindConnection = LdapConnection.createBindConnection(config)) {
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
    try (LdapConnection connection = LdapConnection.createUserConnection(config, userDN, password)) {
      logger.debug("user {} successfully authenticated", userDN);
    }
  }

  private User createUser(Attributes attributes) {
    User user = new User();

    user.setType(LdapRealm.TYPE);

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
    }

    if (!user.isValid()) {
      throw new InvalidUserException("invalid user object: " + user.toString(), user);
    }

    return user;
  }
}
