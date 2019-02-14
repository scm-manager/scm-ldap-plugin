//@flow

import React from "react";
import {Button, Checkbox, Configuration, InputField, Select} from "@scm-manager/ui-components";
import {translate} from "react-i18next";
import TestConnectionDialog from "./TestConnectionDialog";

type LdapConfiguration = {
  profile: string,
  attributeNameId: string,
  attributeNameFullname: string,
  attributeNameMail: string,
  attributeNameGroup: string,
  baseDn: string,
  connectionDn: string,
  connectionPassword: string,
  hostUrl: string,
  searchFilter: string,
  searchFilterGroup: string,
  searchScope: string,
  unitPeople: string,
  unitGroup: string,
  referralStrategy: string,
  enableNestedADGroups: boolean,
  enableStartTls: boolean,
  enabled: boolean
}

type Props = {
  initialConfiguration: Configuration,
  readOnly: boolean,
  onConfigurationChange: (Configuration, boolean) => void,
  t: (string) => string
}

type State = LdapConfiguration & {
  activeFields: string[],
  showTestDialog: boolean
}

class LdapConfigurationForm extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration,
      activeFields: [],
      showTestDialog: false
    };
  }

  valueChangeHandler = (value: any, name: string) => {
    this.setState({
      [name]: value
    }, () => this.props.onConfigurationChange({...this.state}, true));
  };

  profileChangedHandler = (value: string) => {
    const profile = this.profiles[value];
    const fields = Object.keys(profile);
    this.setState({profile: value, activeFields: fields, ...profile},
      () => this.props.onConfigurationChange({...this.state}, true))
  };

  render(): React.ReactNode {
    const {t} = this.props;
    const profiles = Object.keys(this.profiles);

    const testDialog = this.state.showTestDialog ? (
      <TestConnectionDialog
        config={this.state}
        testLink={this.props.initialConfiguration._links.test.href}
        onClose={() => this.setState({showTestDialog: false})}
      />
    ) : null;

    return (
      <>
        <Select name="profile"
                label={t("scm-ldap-plugin.form.profile")}
                helpText={t("scm-ldap-plugin.form.profileHelp")}
                value={this.state.profile}
                options={this.createOptions("profile", profiles)}
                onChange={this.profileChangedHandler}/>
        {this.createInputField("attributeNameId")}
        {this.createInputField("attributeNameFullname")}
        {this.createInputField("attributeNameMail")}
        {this.createInputField("attributeNameGroup")}
        {this.createInputField("baseDn")}
        {this.createInputField("connectionDn")}
        {this.createInputField("connectionPassword", "password")}
        {this.createInputField("hostUrl")}
        {this.createInputField("searchFilter")}
        {this.createInputField("searchFilterGroup")}
        {this.createDropDown("searchScope", ["object", "one", "sub"])}
        {this.createInputField("unitPeople")}
        {this.createInputField("unitGroup")}
        {this.createDropDown("referralStrategy", ["FOLLOW", "IGNORE", "THROW"])}
        {this.createCheckbox("enableNestedADGroups")}
        {this.createCheckbox("enableStartTls")}
        {this.createCheckbox("enabled")}
        <Button label={t("scm-ldap-plugin.form.testButton")} disabled={!this.props.initialConfiguration._links.test} action={this.testConnection}/>
        {testDialog}
      </>
    );
  }

  testConnection = () => {
    this.setState({showTestDialog: true});
  };

  createDropDown = (name: string, options: string[], handler = this.valueChangeHandler) => {
    const {t} = this.props;
    return this.ifActive(name, (<Select name={name}
                                   label={t("scm-ldap-plugin.form." + name)}
                                   helpText={t("scm-ldap-plugin.form." + name + "Help")}
                                   value={this.state[name]}
                                   options={this.createOptions(name, options)}
                                   onChange={handler}/>));
  };

  createOptions = (name: string, options: string[]) => {
    const {t} = this.props;
    return options.map(value => {
      return {
        value: value,
        label: t("scm-ldap-plugin.form.options." + name + "." + value)
      };
    })
  };

  createInputField = (name: string, type: string = "text") => {
    const {t, readOnly} = this.props;
    return this.ifActive(name, (<InputField name={name}
                                       label={t("scm-ldap-plugin.form." + name)}
                                       helpText={t("scm-ldap-plugin.form." + name + "Help")}
                                       disabled={readOnly}
                                       value={this.state[name]}
                                       type={type}
                                       onChange={this.valueChangeHandler}/>));
  };

  createCheckbox = (name: string) => {
    const {t, readOnly} = this.props;
    return this.ifActive(name, (<Checkbox name={name}
                                     label={t("scm-ldap-plugin.form." + name)}
                                     helpText={t("scm-ldap-plugin.form." + name + "Help")}
                                     checked={this.state[name]}
                                     disabled={readOnly}
                                     onChange={this.valueChangeHandler}/>));
  };

  ifActive = (name: string, component: any) => {
    if (this.state.activeFields.includes(name)) {
      return null;
    } else {
      return component;
    }
  };

  profiles = {
    'AD': {
      attributeNameId: 'sAMAccountName',
      attributeNameFullname: 'cn',
      attributeNameMail: 'mail',
      attributeNameGroup: 'memberOf',
      searchFilter: '(&(objectClass=Person)(sAMAccountName={0}))',
      searchFilterGroup: '(&(objectClass=group)(member={0}))',
      searchScope: 'sub',
      unitPeople: '',
      unitGroups: '',
      referralStrategy: 'FOLLOW'
    },
    'Apache': {
      attributeNameId: 'cn',
      attributeNameFullname: 'displayName',
      attributeNameMail: 'mail',
      attributeNameGroup: 'memberOf',
      searchFilter: '(&(objectClass=inetOrgPerson)(cn={0}))',
      searchFilterGroup: '(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))',
      searchScope: 'sub',
      unitPeople: 'ou=People',
      unitGroups: 'ou=Groups',
      enableNestedADGroups: 'false',
      referralStrategy: 'FOLLOW'
    },
    'OpenDJ': {
      attributeNameId: 'uid',
      attributeNameFullname: 'cn',
      attributeNameMail: 'mail',
      attributeNameGroup: 'memberOf',
      searchFilter: '(&(objectClass=inetOrgPerson)(uid={0}))',
      searchFilterGroup: '(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))',
      searchScope: 'sub',
      unitPeople: 'ou=People',
      unitGroups: 'ou=Groups',
      referralStrategy: 'FOLLOW',
      enableNestedADGroups: 'false'
    },
    'OpenLDAP': {
      attributeNameId: 'uid',
      attributeNameFullname: 'cn',
      attributeNameMail: 'mail',
      attributeNameGroup: 'memberOf',
      searchFilter: '(&(objectClass=inetOrgPerson)(uid={0}))',
      searchFilterGroup: '(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))',
      searchScope: 'sub',
      unitPeople: 'ou=People',
      unitGroups: 'ou=Groups',
      referralStrategy: 'FOLLOW',
      enableNestedADGroups: 'false'
    },
    'posix': {
      attributeNameId: 'uid',
      attributeNameFullname: 'cn',
      attributeNameMail: 'mail',
      attributeNameGroup: 'memberOf',
      searchFilter: '(&(objectClass=posixAccount)(uid={0}))',
      searchFilterGroup: '(&(objectClass=posixGroup)(memberUid={1}))',
      searchScope: 'sub',
      unitPeople: 'ou=People',
      unitGroups: 'ou=Groups',
      referralStrategy: 'FOLLOW',
      enableNestedADGroups: 'false'
    },
    'sun': {
      attributeNameId: 'uid',
      attributeNameFullname: 'cn',
      attributeNameMail: 'mail',
      attributeNameGroup: 'memberOf',
      searchFilter: '(&(objectClass=inetOrgPerson)(uid={0}))',
      searchFilterGroup: '(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))',
      searchScope: 'sub',
      unitPeople: 'ou=People',
      unitGroups: 'ou=Groups',
      referralStrategy: 'FOLLOW',
      enableNestedADGroups: 'false'
    },
    'Custom': {}
  };
}

export default translate("plugins")(LdapConfigurationForm);
