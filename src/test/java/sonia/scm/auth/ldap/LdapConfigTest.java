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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Sdorra
 */
public class LdapConfigTest {

  /**
   * Method description
   *
   */
  @Test
  public void testIsValid() {
    LdapConfig config = new LdapConfig();

    config.setAttributeNameId("uid");
    config.setAttributeNameFullname("cn");
    config.setAttributeNameMail("mail");
    config.setBaseDn("dc=scm-manager,dc=org");
    config.setSearchFilter("(uid={0})");
    config.setHostUrl("ldap://localhost:389");
    assertTrue(config.isValid());
    config.setAttributeNameId(null);
    assertFalse(config.isValid());
    config.setAttributeNameId("");
    assertFalse(config.isValid());
  }
}
