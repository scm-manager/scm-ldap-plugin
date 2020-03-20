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
