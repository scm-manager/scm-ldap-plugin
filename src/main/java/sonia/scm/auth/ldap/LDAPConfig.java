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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Thorsten Ludewig
 */
@XmlRootElement(name = "ldap-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class LDAPConfig implements Validateable
{

  /**
   * Method description
   *
   *
   * @return
   */
  public String getAttributeNameFullname()
  {
    return attributeNameFullname;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getAttributeNameGroup()
  {
    return attributeNameGroup;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getAttributeNameId()
  {
    return attributeNameId;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getAttributeNameMail()
  {
    return attributeNameMail;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getBaseDn()
  {
    return baseDn;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getConnectionDn()
  {
    return connectionDn;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getConnectionPassword()
  {
    return connectionPassword;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getHostUrl()
  {
    return hostUrl;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getProfile()
  {
    return profile;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public ReferralStrategy getReferralStrategy()
  {
    return referralStrategy;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getSearchFilter()
  {
    return searchFilter;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getSearchFilterGroup()
  {
    return searchFilterGroup;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getSearchScope()
  {
    return searchScope;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUnitGroup()
  {
    return unitGroup;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUnitPeople()
  {
    return unitPeople;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isEnableNestedADGroups()
  {
    return enableNestedADGroups;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isEnableStartTls()
  {
    return enableStartTls;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isEnabled()
  {
    return enabled;
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
    return isValid(attributeNameId, attributeNameFullname, attributeNameMail,
      hostUrl, searchFilter, searchScope);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param attributeNameFullname
   */
  public void setAttributeNameFullname(String attributeNameFullname)
  {
    this.attributeNameFullname = attributeNameFullname;
  }

  /**
   * Method description
   *
   *
   * @param attributeNameGroup
   */
  public void setAttributeNameGroup(String attributeNameGroup)
  {
    this.attributeNameGroup = attributeNameGroup;
  }

  /**
   * Method description
   *
   *
   * @param attributeNameId
   */
  public void setAttributeNameId(String attributeNameId)
  {
    this.attributeNameId = attributeNameId;
  }

  /**
   * Method description
   *
   *
   * @param attributeNameMail
   */
  public void setAttributeNameMail(String attributeNameMail)
  {
    this.attributeNameMail = attributeNameMail;
  }

  /**
   * Method description
   *
   *
   * @param baseDn
   */
  public void setBaseDn(String baseDn)
  {
    this.baseDn = baseDn;
  }

  /**
   * Method description
   *
   *
   * @param connectionDn
   */
  public void setConnectionDn(String connectionDn)
  {
    this.connectionDn = connectionDn;
  }

  /**
   * Method description
   *
   *
   * @param connectionPassword
   */
  public void setConnectionPassword(String connectionPassword)
  {
    this.connectionPassword = connectionPassword;
  }

  /**
   * Method description
   *
   *
   * @param enableNestedADGroups
   */
  public void setEnableNestedADGroups(boolean enableNestedADGroups)
  {
    this.enableNestedADGroups = enableNestedADGroups;
  }

  /**
   * Method description
   *
   *
   * @param enableStartTls
   */
  public void setEnableStartTls(boolean enableStartTls)
  {
    this.enableStartTls = enableStartTls;
  }

  /**
   * Method description
   *
   *
   *
   * @param enabled
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
  }

  /**
   * Method description
   *
   *
   * @param hostUrl
   */
  public void setHostUrl(String hostUrl)
  {
    this.hostUrl = hostUrl;
  }

  /**
   * Method description
   *
   *
   * @param profile
   */
  public void setProfile(String profile)
  {
    this.profile = profile;
  }

  /**
   * Method description
   *
   *
   * @param referralStrategy
   */
  public void setReferralStrategy(ReferralStrategy referralStrategy)
  {
    this.referralStrategy = referralStrategy;
  }

  /**
   * Method description
   *
   *
   * @param searchFilter
   */
  public void setSearchFilter(String searchFilter)
  {
    this.searchFilter = searchFilter;
  }

  /**
   * Method description
   *
   *
   * @param searchFilterGroup
   */
  public void setSearchFilterGroup(String searchFilterGroup)
  {
    this.searchFilterGroup = searchFilterGroup;
  }

  /**
   * Method description
   *
   *
   * @param searchScope
   */
  public void setSearchScope(String searchScope)
  {
    this.searchScope = searchScope;
  }

  /**
   * Method description
   *
   *
   * @param unitGroup
   */
  public void setUnitGroup(String unitGroup)
  {
    this.unitGroup = unitGroup;
  }

  /**
   * Method description
   *
   *
   * @param unitPeople
   */
  public void setUnitPeople(String unitPeople)
  {
    this.unitPeople = unitPeople;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param fields
   *
   * @return
   */
  private boolean isValid(String... fields)
  {
    boolean valid = true;

    for (String field : fields)
    {
      if (Util.isEmpty(field))
      {
        valid = false;

        break;
      }
    }

    return valid;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "attribute-name-fullname")
  private String attributeNameFullname = "cn";

  /** Field description */
  @XmlElement(name = "attribute-name-group")
  private String attributeNameGroup = "group";

  /** Field description */
  @XmlElement(name = "attribute-name-id")
  private String attributeNameId = "uid";

  /** Field description */
  @XmlElement(name = "attribute-name-mail")
  private String attributeNameMail = "mail";

  /** Field description */
  @XmlElement(name = "base-dn")
  private String baseDn = "dc=scm-manager,dc=org";

  /** Field description */
  @XmlElement(name = "connection-dn")
  private String connectionDn = "cn=Directory Manager";

  /** Field description */
  @XmlElement(name = "connection-password")
  private String connectionPassword = "password";

  /** Field description */
  @XmlElement(name = "host-url")
  private String hostUrl = "ldap://localhost:389";

  /** Field description */
  @XmlElement(name = "profile")
  private String profile = "Custom";

  /** Field description */
  @XmlElement(name = "referral-strategy")
  private ReferralStrategy referralStrategy = ReferralStrategy.FOLLOW;

  /** Field description */
  @XmlElement(name = "search-filter")
  private String searchFilter = "(&(uid={0})(objectClass=posixAccount))";

  /** Field description */
  @XmlElement(name = "search-filter-group")
  private String searchFilterGroup =
    "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))";

  /** Field description */
  @XmlElement(name = "search-scope")
  private String searchScope = "one";

  /** Field description */
  @XmlElement(name = "unit-groups")
  private String unitGroup = "ou=Groups";

  /** Field description */
  @XmlElement(name = "unit-people")
  private String unitPeople = "ou=People";

  /** Field description */
  @XmlElement(name = "enabled")
  private boolean enabled = false;

  /** Field description */
  @XmlElement(name = "enable-starttls")
  private boolean enableStartTls = false;

  /** Field description */
  @XmlElement(name = "enable-nested-ad-groups")
  private boolean enableNestedADGroups = false;
}
