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

import sonia.scm.Validateable;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Thorsten Ludewig
 */
@XmlRootElement(name = "ldap-config-list")
@XmlAccessorType(XmlAccessType.FIELD)
public class LDAPConfigList implements Validateable
{
  public LDAPConfigList() {
    ldap_configs = new ArrayList<LDAPConfig>();
    LDAPConfig config = new LDAPConfig();
    StringBuilder hostUrl = new StringBuilder("");
    hostUrl.append("ldap://localhost:389");
    config.setUniqueId("LDAP1");
    config.setEnabled(false);
    config.setHostUrl(hostUrl.toString());
    config.setAttributeNameFullname("cn");
    config.setAttributeNameGroup("group");
    config.setAttributeNameId("uid");
    config.setAttributeNameMail("mail");
    config.setBaseDn("dc=scm-manager,dc=org");
    config.setConnectionDn("cn=Directory Manager");
    config.setConnectionPassword("examplepassword");
    config.setEnableNestedADGroups(false);
    config.setHostUrl(hostUrl.toString());
    config.setProfile("Custom");
    config.setReferralStrategy(ReferralStrategy.FOLLOW);
    config.setSearchFilter("(&(uid={0})(objectClass=posixAccount))");
    config.setSearchFilterGroup("(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))");
    config.setSearchScope("one");
    config.setUnitGroup("ou=Groups");
    config.setUnitPeople("ou=People");
    ldap_configs.add(config);
  }
  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isValid()
  {
    for (LDAPConfig ldap_config : ldap_configs) {
      // If any one of the configs is not valid then return the list as invalid.
      if (!ldap_config.isValid()) {
        return false;
      }
    }

    // If we have reached here then all configurations must be valid.
    return  true;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param ldap_configs
   */
  public void setLDAPConfigList(List<LDAPConfig> ldap_configs)
  {
    this.ldap_configs = ldap_configs;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<LDAPConfig> getLDAPConfigList()
  {
    return ldap_configs;
  }

  //~--- fields ---------------------------------------------------------------

    /** Field description */
    @XmlElementWrapper(name="ldap-configs")
    @XmlElement(name="ldap-config")
    List<LDAPConfig> ldap_configs;

}
