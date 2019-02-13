//@flow

import React from "react";
import {Checkbox, Configuration, DropDown, InputField, Select} from "@scm-manager/ui-components";
import {translate} from "react-i18next";

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
  configurationChanged: boolean
}

class LdapConfigurationForm extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  valueChangeHandler = (value: string, name: string) => {
    this.setState({
      [name]: value
    }, () => this.props.onConfigurationChange({...this.state}, true));
  };

  render(): React.ReactNode {
    return (
      <>
        {this.createDropDown("profile", ["Custom"])}
        {this.createInputField("attributeNameId")}
        {this.createInputField("attributeNameFullname")}
        {this.createInputField("attributeNameMail")}
        {this.createInputField("attributeNameGroup")}
        {this.createInputField("baseDn")}
        {this.createInputField("connectionDn")}
        {this.createInputField("connectionPassword")}
        {this.createInputField("hostUrl")}
        {this.createInputField("searchFilter")}
        {this.createInputField("searchFilterGroup")}
        {this.createDropDown("searchScope", ["one"])}
        {this.createInputField("unitPeople")}
        {this.createInputField("unitGroup")}
        {this.createDropDown("referralStrategy", ["FOLLOW"])}
        {this.createCheckbox("enableNestedADGroups")}
        {this.createCheckbox("enableStartTls")}
        {this.createCheckbox("enabled")}
      </>
    );
  }

  createDropDown = (name: string, options: string[]) => {
    const {t} = this.props;
    return (<Select name={name}
                    label={t("scm-ldap-plugin.form." + name)}
                    helpText={t("scm-ldap-plugin.form." + name + "Help")}
                    value={options[0]}
                    options={options.map(value => {
                      return {
                        value: value,
                        label: t("scm-ldap-plugin.form.options." + name + "." + value)
                      };
                    })}
                    onChange={this.valueChangeHandler}/>);
  };

  createInputField = (name: string) => {
    const {t, readOnly} = this.props;
    return (<InputField name={name}
                        label={t("scm-ldap-plugin.form." + name)}
                        helpText={t("scm-ldap-plugin.form." + name + "Help")}
                        disabled={readOnly}
                        value={this.state[name]}
                        onChange={this.valueChangeHandler}/>);
  };

  createCheckbox = (name: string) => {
    const {t, readOnly} = this.props;
    return (<Checkbox name={name}
                      label={t("scm-ldap-plugin.form." + name)}
                      helpText={t("scm-ldap-plugin.form." + name + "Help")}
                      checked={this.state[name]}
                      disabled={readOnly}
                      onChange={this.valueChangeHandler}/>);
  };
}

export default translate("plugins")(LdapConfigurationForm);
