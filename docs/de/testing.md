---
title: Testen
subtitle: Testumgebung einrichten
---
Um dieses Plugin gegen ein LDAP zu testen, kann ein [vorkonfiguriertes LDAP in einem Docker-Container](https://github.com/rroemhild/docker-test-openldap) verwendet werden:

```
docker pull rroemhild/test-openldap
docker run --privileged -d -p 389:389 rroemhild/test-openldap
```

Oder verwende `docker-compose`:

```
docker-compose up
```

Um eine Verbindung zu diesem LDAP-Container herzustellen, muss man in der globalen LDAP-Konfiguration die folgenden Einstellungen vornehmen:

* Wähle Profil => `Benutzerdefiniert`
* Setze Server URL auf `ldap://localhost:10389`
* Setze Base DN auf `dc=planetexpress,dc=com`
* Setze Verbindungs DN auf `cn=admin,dc=planetexpress,dc=com`
* Setze Verbindungspasswort auf `GoodNewsEveryone`
* Setze Suchfilter auf `(&(objectClass=inetOrgPerson)(uid={0}))`
* Setze Gruppensuchfilter auf `(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))`

Es kann ein einzelner `curl`-Request verwendet werden, um diese Konfiguration anzuwenden:

```bash
curl -u scmadmin:scmadmin \
     --data '{"attributeNameFullname":"cn","attributeNameGroup":"group","attributeNameId":"uid","attributeNameMail":"mail","baseDn":"dc=planetexpress,dc=com","connectionDn":"cn=admin,dc=planetexpress,dc=com","connectionPassword":"__DUMMY__","hostUrl":"ldap://localhost:389","profile":"Custom","referralStrategy":"FOLLOW","searchFilter":"(&(objectClass=inetOrgPerson)(uid={0}))","searchFilterGroup":"(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))","searchFilterNestedGroup":"(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))","searchScope":"one","unitGroup":"ou=Groups","unitPeople":"ou=People","enabled":true,"enableStartTls":false,"enableNestedADGroups":false,"enableNestedGroups":false,"activeFields":[],"showTestDialog":false}' \
     -H "Content-Type: application/json" \
     -X PUT \
     http://localhost:8081/scm/api/v2/config/ldap
```

Nun kann die Verbindung mit Benutzername `professor` und Passwort `professor` getestet werden.
