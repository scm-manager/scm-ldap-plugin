package sonia.scm.auth.ldap;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@Getter
@Setter
public class LDAPConfigDto {
    private String attributeNameFullname;
    private String attributeNameGroup;
    private String attributeNameId;
    private String attributeNameMail;
    private String baseDn;
    private String connectionDn;
    private String connectionPassword;
    private String hostUrl;
    private String profile;
    private String referralStrategy;
    private String searchFilter;
    private String searchFilterGroup;
    private String searchScope;
    private String unitGroup;
    private String unitPeople;
    private boolean enabled;
    private boolean enableStartTls;
    private boolean enableNestedADGroups;
}
