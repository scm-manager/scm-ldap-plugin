///
/// MIT License
///
/// Copyright (c) 2020-present Cloudogu GmbH and Contributors
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in all
/// copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
/// SOFTWARE.
///

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
