# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 2.3.0
### Added
- Option to replace invalid characters in group names ([#19](https://github.com/scm-manager/scm-ldap-plugin/pull/19))
### Fixed
- Calculate corret DN for nested group search filter ([#20](https://github.com/scm-manager/scm-ldap-plugin/pull/20))

## 2.2.0 - 2021-03-01
### Changed
- Cache authentication info ([#15](https://github.com/scm-manager/scm-ldap-plugin/pull/15))

## 2.1.0 - 2021-02-15
### Added
- Nested group support for non-AD servers ([#13](https://github.com/scm-manager/scm-ldap-plugin/pull/13))

## 2.0.1 - 2020-10-07
### Fixed
- Ignore invalid mail address from ldap ([#7](https://github.com/scm-manager/scm-ldap-plugin/pull/7))
- Return empty list of groups on error instead of throwing exception ([#9](https://github.com/scm-manager/scm-ldap-plugin/pull/9))

## 2.0.0 - 2020-06-04
### Changed
- Changeover to MIT license ([#3](https://github.com/scm-manager/scm-ldap-plugin/pull/3))
- Rebuild for api changes from core

## 2.0.0-rc2 - 2020-03-13
### Added
- Add swagger rest annotations to generate openAPI specs for the scm-openapi-plugin. ([#2](https://github.com/scm-manager/scm-ldap-plugin/pull/2))
