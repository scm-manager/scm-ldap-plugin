// @flow

export const PROFILES = {
  AD: {
    attributeNameId: "sAMAccountName",
    attributeNameFullname: "cn",
    attributeNameMail: "mail",
    attributeNameGroup: "memberOf",
    searchFilter: "(&(objectClass=Person)(sAMAccountName={0}))",
    searchFilterGroup: "(&(objectClass=group)(member={0}))",
    searchScope: "sub",
    unitPeople: "",
    unitGroups: "",
    referralStrategy: "FOLLOW"
  },
  Apache: {
    attributeNameId: "cn",
    attributeNameFullname: "displayName",
    attributeNameMail: "mail",
    attributeNameGroup: "memberOf",
    searchFilter: "(&(objectClass=inetOrgPerson)(cn={0}))",
    searchFilterGroup: "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))",
    searchScope: "sub",
    unitPeople: "ou=People",
    unitGroups: "ou=Groups",
    enableNestedADGroups: "false",
    referralStrategy: "FOLLOW"
  },
  OpenDJ: {
    attributeNameId: "uid",
    attributeNameFullname: "cn",
    attributeNameMail: "mail",
    attributeNameGroup: "memberOf",
    searchFilter: "(&(objectClass=inetOrgPerson)(uid={0}))",
    searchFilterGroup: "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))",
    searchScope: "sub",
    unitPeople: "ou=People",
    unitGroups: "ou=Groups",
    referralStrategy: "FOLLOW",
    enableNestedADGroups: "false"
  },
  OpenLDAP: {
    attributeNameId: "uid",
    attributeNameFullname: "cn",
    attributeNameMail: "mail",
    attributeNameGroup: "memberOf",
    searchFilter: "(&(objectClass=inetOrgPerson)(uid={0}))",
    searchFilterGroup: "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))",
    searchScope: "sub",
    unitPeople: "ou=People",
    unitGroups: "ou=Groups",
    referralStrategy: "FOLLOW",
    enableNestedADGroups: "false"
  },
  posix: {
    attributeNameId: "uid",
    attributeNameFullname: "cn",
    attributeNameMail: "mail",
    attributeNameGroup: "memberOf",
    searchFilter: "(&(objectClass=posixAccount)(uid={0}))",
    searchFilterGroup: "(&(objectClass=posixGroup)(memberUid={1}))",
    searchScope: "sub",
    unitPeople: "ou=People",
    unitGroups: "ou=Groups",
    referralStrategy: "FOLLOW",
    enableNestedADGroups: "false"
  },
  sun: {
    attributeNameId: "uid",
    attributeNameFullname: "cn",
    attributeNameMail: "mail",
    attributeNameGroup: "memberOf",
    searchFilter: "(&(objectClass=inetOrgPerson)(uid={0}))",
    searchFilterGroup: "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))",
    searchScope: "sub",
    unitPeople: "ou=People",
    unitGroups: "ou=Groups",
    referralStrategy: "FOLLOW",
    enableNestedADGroups: "false"
  },
  Custom: {}
};
