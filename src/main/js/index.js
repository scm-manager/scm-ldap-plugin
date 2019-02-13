// @flow

import {ConfigurationBinder as cfgBinder} from "@scm-manager/ui-components"
import LdapConfiguration from "./LdapConfiguration";

cfgBinder.bindGlobal("/ldap", "scm-ldap-plugin.nav-link", "ldapConfig", LdapConfiguration);
