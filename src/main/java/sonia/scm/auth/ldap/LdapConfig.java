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

import sonia.scm.Validateable;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Thorsten Ludewig
 */
@XmlRootElement(name = "ldap-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class LdapConfig implements Validateable {

  @XmlElement(name = "attribute-name-fullname")
  private String attributeNameFullname = "cn";

  @XmlElement(name = "attribute-name-group")
  private String attributeNameGroup = "group";

  @XmlElement(name = "attribute-name-id")
  private String attributeNameId = "uid";

  @XmlElement(name = "attribute-name-mail")
  private String attributeNameMail = "mail";

  @XmlElement(name = "base-dn")
  private String baseDn = "dc=scm-manager,dc=org";

  @XmlElement(name = "connection-dn")
  private String connectionDn = "cn=Directory Manager";

  @SuppressWarnings("squid:S2068") // suppress hardcoded password
  @XmlElement(name = "connection-password")
  @XmlJavaTypeAdapter(XmlCipherAdapter.class)
  private String connectionPassword = "password";

  @XmlElement(name = "host-url")
  private String hostUrl = "ldap://localhost:389";

  @XmlElement(name = "profile")
  private String profile = "Custom";

  @XmlElement(name = "referral-strategy")
  private ReferralStrategy referralStrategy = ReferralStrategy.FOLLOW;

  @XmlElement(name = "search-filter")
  private String searchFilter = "(&(uid={0})(objectClass=posixAccount))";

  @XmlElement(name = "search-filter-group")
  private String searchFilterGroup =
    "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))";

  @XmlElement(name = "search-filter-nested-group")
  private String searchFilterNestedGroup =
    "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))";

  @XmlElement(name = "search-scope")
  private String searchScope = "one";

  @XmlElement(name = "unit-groups")
  private String unitGroup = "ou=Groups";

  @XmlElement(name = "unit-people")
  private String unitPeople = "ou=People";

  @XmlElement(name = "enabled")
  private boolean enabled = false;

  @XmlElement(name = "enable-starttls")
  private boolean enableStartTls = false;

  @XmlElement(name = "enable-nested-ad-groups")
  private boolean enableNestedADGroups = false;

  @XmlElement(name = "enable-nested-groups")
  private boolean enableNestedGroups = false;

  @XmlElement(name = "remove-illegal-characters")
  private boolean removeInvalidCharacters = false;

  public String getAttributeNameFullname() {
    return attributeNameFullname;
  }

  public String getAttributeNameGroup() {
    return attributeNameGroup;
  }

  public String getAttributeNameId() {
    return attributeNameId;
  }

  public String getAttributeNameMail() {
    return attributeNameMail;
  }

  public String getBaseDn() {
    return baseDn;
  }

  public String getConnectionDn() {
    return connectionDn;
  }

  public String getConnectionPassword() {
    return connectionPassword;
  }

  public String getHostUrl() {
    return hostUrl;
  }

  public String getProfile() {
    return profile;
  }

  public ReferralStrategy getReferralStrategy() {
    return referralStrategy;
  }

  public String getSearchFilter() {
    return searchFilter;
  }

  public String getSearchFilterGroup() {
    return searchFilterGroup;
  }

  public String getSearchFilterNestedGroup() {
    return searchFilterNestedGroup;
  }

  public String getSearchScope() {
    return searchScope;
  }

  public String getUnitGroup() {
    return unitGroup;
  }

  public String getUnitPeople() {
    return unitPeople;
  }

  public boolean isEnableNestedADGroups() {
    return enableNestedADGroups;
  }

  public boolean isEnableNestedGroups() {
    return enableNestedGroups;
  }

  public boolean isEnableStartTls() {
    return enableStartTls;
  }

  public boolean isRemoveInvalidCharacters() {
    return removeInvalidCharacters;
  }

  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean isValid() {
    return isValid(attributeNameId, attributeNameFullname, attributeNameMail,
      hostUrl, searchFilter, searchScope);
  }

  public void setAttributeNameFullname(String attributeNameFullname) {
    this.attributeNameFullname = attributeNameFullname;
  }

  public void setAttributeNameGroup(String attributeNameGroup) {
    this.attributeNameGroup = attributeNameGroup;
  }

  public void setAttributeNameId(String attributeNameId) {
    this.attributeNameId = attributeNameId;
  }

  public void setAttributeNameMail(String attributeNameMail) {
    this.attributeNameMail = attributeNameMail;
  }

  public void setBaseDn(String baseDn) {
    this.baseDn = baseDn;
  }

  public void setConnectionDn(String connectionDn) {
    this.connectionDn = connectionDn;
  }

  public void setConnectionPassword(String connectionPassword) {
    this.connectionPassword = connectionPassword;
  }

  public void setEnableNestedADGroups(boolean enableNestedADGroups) {
    this.enableNestedADGroups = enableNestedADGroups;
  }

  public void setEnableNestedGroups(boolean enableNestedGroups) {
    this.enableNestedGroups = enableNestedGroups;
  }

  public void setEnableStartTls(boolean enableStartTls) {
    this.enableStartTls = enableStartTls;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void setHostUrl(String hostUrl) {
    this.hostUrl = hostUrl;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public void setReferralStrategy(ReferralStrategy referralStrategy) {
    this.referralStrategy = referralStrategy;
  }

  public void setSearchFilter(String searchFilter) {
    this.searchFilter = searchFilter;
  }

  public void setSearchFilterGroup(String searchFilterGroup) {
    this.searchFilterGroup = searchFilterGroup;
  }

  public void setSearchFilterNestedGroup(String searchFilterNestedGroup) {
    this.searchFilterNestedGroup = searchFilterNestedGroup;
  }

  public void setSearchScope(String searchScope) {
    this.searchScope = searchScope;
  }

  public void setUnitGroup(String unitGroup) {
    this.unitGroup = unitGroup;
  }

  public void setUnitPeople(String unitPeople) {
    this.unitPeople = unitPeople;
  }

  public void setRemoveInvalidCharacters(boolean removeInvalidCharacters) {
    this.removeInvalidCharacters = removeInvalidCharacters;
  }

  private boolean isValid(String... fields) {
    boolean valid = true;

    for (String field : fields) {
      if (Util.isEmpty(field)) {
        valid = false;

        break;
      }
    }

    return valid;
  }
}
