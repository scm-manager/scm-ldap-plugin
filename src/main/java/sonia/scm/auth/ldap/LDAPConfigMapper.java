package sonia.scm.auth.ldap;

import org.apache.commons.lang.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import javax.inject.Inject;

@Mapper
public abstract class LDAPConfigMapper {

    private static final String DUMMY_PASSWORD = "__DUMMY__";

    public abstract LDAPConfigDto map(LDAPConfig config);

    public abstract LDAPConfig map(LDAPConfigDto dto, @Context LDAPConfig oldConfig);

    @AfterMapping
    void replaceDummyWithOldPassword(@MappingTarget LDAPConfig target, @Context LDAPConfig oldConfiguration) {
        if (DUMMY_PASSWORD.equals(target.getConnectionPassword())) {
            target.setConnectionPassword(oldConfiguration.getConnectionPassword());
        }
    }

    @AfterMapping
    void replacePasswordWithDummy(@MappingTarget LDAPConfigDto target) {
        if (StringUtils.isNotEmpty(target.getConnectionPassword())) {
            target.setConnectionPassword(DUMMY_PASSWORD);
        }
    }
}
