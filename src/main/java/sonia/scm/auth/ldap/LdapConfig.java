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

import sonia.scm.Validateable;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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

  @XmlElement(name = "exclude-groups-outside-unit")
  private boolean excludeGroupsOutsideUnit = false;

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

  public boolean isExcludeGroupsOutsideUnit() {
    return excludeGroupsOutsideUnit;
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

  public void setExcludeGroupsOutsideUnit(boolean excludeGroupsOutsideUnit) {
    this.excludeGroupsOutsideUnit = excludeGroupsOutsideUnit;
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
