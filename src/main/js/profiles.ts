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

export const PROFILES = {
  AD: {
    attributeNameId: "sAMAccountName",
    attributeNameFullname: "cn",
    attributeNameMail: "mail",
    attributeNameGroup: "memberOf",
    searchFilter: "(&(objectClass=Person)(sAMAccountName={0}))",
    searchFilterGroup: "(&(objectClass=group)(member={0}))",
    searchFilterNestedGroup: "",
    searchScope: "sub",
    unitPeople: "",
    unitGroup: "",
    enableNestedADGroups: true,
    enableNestedGroups: false,
    excludeGroupsOutsideUnit: false,
    referralStrategy: "FOLLOW"
  },
  Apache: {
    attributeNameId: "cn",
    attributeNameFullname: "displayName",
    attributeNameMail: "mail",
    attributeNameGroup: "memberOf",
    searchFilter: "(&(objectClass=inetOrgPerson)(cn={0}))",
    searchFilterGroup: "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))",
    searchFilterNestedGroup: "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))",
    searchScope: "sub",
    unitPeople: "ou=People",
    unitGroup: "ou=Groups",
    enableNestedADGroups: false,
    enableNestedGroups: false,
    excludeGroupsOutsideUnit: false,
    referralStrategy: "FOLLOW"
  },
  OpenDJ: {
    attributeNameId: "uid",
    attributeNameFullname: "cn",
    attributeNameMail: "mail",
    attributeNameGroup: "memberOf",
    searchFilter: "(&(objectClass=inetOrgPerson)(uid={0}))",
    searchFilterGroup: "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))",
    searchFilterNestedGroup: "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))",
    searchScope: "sub",
    unitPeople: "ou=People",
    unitGroup: "ou=Groups",
    enableNestedADGroups: false,
    enableNestedGroups: false,
    excludeGroupsOutsideUnit: false,
    referralStrategy: "FOLLOW",
  },
  OpenLDAP: {
    attributeNameId: "uid",
    attributeNameFullname: "cn",
    attributeNameMail: "mail",
    attributeNameGroup: "memberOf",
    searchFilter: "(&(objectClass=inetOrgPerson)(uid={0}))",
    searchFilterGroup: "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))",
    searchFilterNestedGroup: "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))",
    searchScope: "sub",
    unitPeople: "ou=People",
    unitGroup: "ou=Groups",
    enableNestedADGroups: false,
    enableNestedGroups: false,
    excludeGroupsOutsideUnit: false,
    referralStrategy: "FOLLOW"
  },
  posix: {
    attributeNameId: "uid",
    attributeNameFullname: "cn",
    attributeNameMail: "mail",
    attributeNameGroup: "memberOf",
    searchFilter: "(&(objectClass=posixAccount)(uid={0}))",
    searchFilterGroup: "(&(objectClass=posixGroup)(memberUid={1}))",
    searchFilterNestedGroup: "",
    searchScope: "sub",
    unitPeople: "ou=People",
    unitGroup: "ou=Groups",
    enableNestedADGroups: false,
    enableNestedGroups: false,
    excludeGroupsOutsideUnit: false,
    referralStrategy: "FOLLOW"
  },
  sun: {
    attributeNameId: "uid",
    attributeNameFullname: "cn",
    attributeNameMail: "mail",
    attributeNameGroup: "memberOf",
    searchFilter: "(&(objectClass=inetOrgPerson)(uid={0}))",
    searchFilterGroup: "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))",
    searchFilterNestedGroup: "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))",
    searchScope: "sub",
    unitPeople: "ou=People",
    unitGroup: "ou=Groups",
    enableNestedADGroups: false,
    enableNestedGroups: false,
    excludeGroupsOutsideUnit: false,
    referralStrategy: "FOLLOW"
  },
  Custom: {}
};
