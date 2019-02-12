package sonia.scm.auth.ldap;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
public class LDAPConfigDto {
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
}
