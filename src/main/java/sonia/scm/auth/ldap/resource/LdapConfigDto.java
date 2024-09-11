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

package sonia.scm.auth.ldap.resource;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("squid:S2160") // No need for equals and hash code here, never compared, never hashed
public class LdapConfigDto extends HalRepresentation {
  @NotEmpty
  private String attributeNameFullname;
  private String attributeNameGroup;
  @NotEmpty
  private String attributeNameId;
  @NotEmpty
  private String attributeNameMail;
  private String baseDn;
  private String connectionDn;
  private String connectionPassword;
  @NotEmpty
  private String hostUrl;
  private String profile;
  private String referralStrategy;
  @NotEmpty
  private String searchFilter;
  private String searchFilterGroup;
  private String searchFilterNestedGroup;
  @NotEmpty
  private String searchScope;
  private String unitGroup;
  private String unitPeople;
  private boolean enabled;
  private boolean enableStartTls;
  private boolean enableNestedADGroups;
  private boolean enableNestedGroups;
  private boolean removeInvalidCharacters;
  private boolean excludeGroupsOutsideUnit;

  public LdapConfigDto(Links links) {
    super(links);
  }
}
