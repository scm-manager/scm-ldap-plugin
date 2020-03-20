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
