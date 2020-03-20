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
  @NotEmpty
  private String searchScope;
  private String unitGroup;
  private String unitPeople;
  private boolean enabled;
  private boolean enableStartTls;
  private boolean enableNestedADGroups;

  public LdapConfigDto(Links links) {
    super(links);
  }
}
