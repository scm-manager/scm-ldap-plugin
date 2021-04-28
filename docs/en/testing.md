---
title: Testing
subtitle: Setup Test Environment
---
To test this plugin against a ldap you may use a [preconfigured ldap inside a docker container](https://github.com/rroemhild/docker-test-openldap):

```
docker pull rroemhild/test-openldap
docker run --privileged -d -p 389:389 rroemhild/test-openldap
```

Or simply use `docker-compose`:

```
docker-compose up
```

To connect against this ldap container you must set the following settings in your global ldap configuration:

* Select Profile => `Custom`
* Set Host URL to `ldap://localhost:10389`
* Set Base DN to `dc=planetexpress,dc=com`
* Set Connection DN to `cn=admin,dc=planetexpress,dc=com`
* Set the Connection Password to `GoodNewsEveryone`
* Set search filter to `(&(objectClass=inetOrgPerson)(uid={0}))`
* Set group search filter to `(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))`

You can use a single `curl` request for this configuration:

```bash
curl -u scmadmin:scmadmin \
     --data '{"attributeNameFullname":"cn","attributeNameGroup":"group","attributeNameId":"uid","attributeNameMail":"mail","baseDn":"dc=planetexpress,dc=com","connectionDn":"cn=admin,dc=planetexpress,dc=com","connectionPassword":"__DUMMY__","hostUrl":"ldap://localhost:389","profile":"Custom","referralStrategy":"FOLLOW","searchFilter":"(&(objectClass=inetOrgPerson)(uid={0}))","searchFilterGroup":"(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))","searchFilterNestedGroup":"(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))","searchScope":"one","unitGroup":"ou=Groups","unitPeople":"ou=People","enabled":true,"enableStartTls":false,"enableNestedADGroups":false,"enableNestedGroups":false,"activeFields":[],"showTestDialog":false}' \
     -H "Content-Type: application/json" \
     -X PUT \
     http://localhost:8081/scm/api/v2/config/ldap
```

Now you can test the connection with username `professor` and password `professor`.
