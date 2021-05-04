/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { Button, Checkbox, InputField, Select } from "@scm-manager/ui-components";
import { withTranslation, WithTranslation } from "react-i18next";
import TestConnectionDialog from "./TestConnectionDialog";
import { PROFILES } from "./profiles";

type LdapConfiguration = {
  profile: string;
  attributeNameId: string;
  attributeNameFullname: string;
  attributeNameMail: string;
  attributeNameGroup: string;
  baseDn: string;
  connectionDn: string;
  connectionPassword: string;
  hostUrl: string;
  searchFilter: string;
  searchFilterGroup: string;
  searchFilterNestedGroup: string;
  searchScope: string;
  unitPeople: string;
  unitGroup: string;
  referralStrategy: string;
  enableNestedGroups: boolean;
  enableNestedADGroups: boolean;
  enableStartTls: boolean;
  removeInvalidCharacters: boolean;
  enabled: boolean;
};

type Props = WithTranslation & {
  initialConfiguration: LdapConfiguration;
  readOnly: boolean;
  onConfigurationChange: (config: LdapConfiguration, valid: boolean) => void;
};

type State = LdapConfiguration & {
  activeFields: string[];
  showTestDialog: boolean;
};

class LdapConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration,
      activeFields: [],
      showTestDialog: false
    };
  }

  componentDidMount() {
    this.profileChangedHandler(this.state.profile);
  }

  valueChangeHandler = (value: any, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () =>
        this.props.onConfigurationChange(
          {
            ...this.state
          },
          true
        )
    );
  };

  profileChangedHandler = (value: string) => {
    const profile = PROFILES[value];
    const fields = Object.keys(profile);
    this.setState(
      {
        profile: value,
        activeFields: fields,
        ...profile
      },
      () =>
        this.props.onConfigurationChange(
          {
            ...this.state
          },
          true
        )
    );
  };

  render(): React.ReactNode {
    const { t } = this.props;
    const profileNames = Object.keys(PROFILES);

    const testDialog = this.state.showTestDialog ? (
      <TestConnectionDialog
        config={this.state}
        testLink={this.props.initialConfiguration._links.test.href}
        onClose={() =>
          this.setState({
            showTestDialog: false
          })
        }
      />
    ) : null;

    return (
      <div className="columns is-multiline">
        <div className="column is-full">
          <Select
            name="profile"
            label={t("scm-ldap-plugin.form.profile")}
            helpText={t("scm-ldap-plugin.form.profileHelp")}
            value={this.state.profile}
            options={this.createOptions("profile", profileNames)}
            onChange={this.profileChangedHandler}
          />
        </div>
        {this.createInputField("hostUrl")}
        {this.createInputField("baseDn")}
        {this.createInputField("connectionDn")}
        {this.createInputField("connectionPassword", "password")}
        {this.createInputField("attributeNameId")}
        {this.createInputField("attributeNameFullname")}
        {this.createInputField("attributeNameMail")}
        {this.createInputField("attributeNameGroup")}
        {this.createInputField("unitPeople")}
        {this.createInputField("searchFilter")}
        {this.createInputField("unitGroup")}
        {this.createInputField("searchFilterGroup")}
        {this.createDropDown("searchScope", ["object", "one", "sub"])}
        {this.createDropDown("referralStrategy", ["FOLLOW", "IGNORE", "THROW"])}
        <div className="column is-full">
          {this.createCheckbox("enableNestedGroups")}
        </div>
        {this.createInputField("searchFilterNestedGroup", "text", "is-full", !this.state.enableNestedGroups)}
        <div className="column is-full">
          {this.createCheckbox("enableNestedADGroups")}
          {this.createCheckbox("enableStartTls")}
          {this.createCheckbox("removeInvalidCharacters")}
          {this.createCheckbox("enabled")}
        </div>
        <div className="column is-full">
          <Button
            label={t("scm-ldap-plugin.form.testButton")}
            disabled={!this.props.initialConfiguration._links.test}
            action={this.testConnection}
          />
        </div>
        {testDialog}
      </div>
    );
  }

  testConnection = () => {
    this.setState({
      showTestDialog: true
    });
  };

  createDropDown = (name: string, options: string[], handler = this.valueChangeHandler) => {
    const { t } = this.props;
    return this.ifActive(
      name,
      <div className="column is-half">
        <Select
          name={name}
          label={t("scm-ldap-plugin.form." + name)}
          helpText={t("scm-ldap-plugin.form." + name + "Help")}
          value={this.state[name]}
          options={this.createOptions(name, options)}
          onChange={handler}
        />
      </div>
    );
  };

  createOptions = (name: string, options: string[]) => {
    const { t } = this.props;
    return options.map(value => {
      return {
        value: value,
        label: t("scm-ldap-plugin.form.options." + name + "." + value)
      };
    });
  };

  createInputField = (name: string, type = "text", className: string = "is-half", disabled: boolean = false) => {
    const { t, readOnly } = this.props;
    return this.ifActive(
      name,
      <div className={`column ${className}`}>
        <InputField
          name={name}
          label={t("scm-ldap-plugin.form." + name)}
          helpText={t("scm-ldap-plugin.form." + name + "Help")}
          disabled={readOnly || disabled}
          value={this.state[name]}
          type={type}
          onChange={this.valueChangeHandler}
        />
      </div>
    );
  };

  createCheckbox = (name: string) => {
    const { t, readOnly } = this.props;
    return this.ifActive(
      name,
      <Checkbox
        name={name}
        label={t("scm-ldap-plugin.form." + name)}
        helpText={t("scm-ldap-plugin.form." + name + "Help")}
        checked={this.state[name]}
        disabled={readOnly}
        onChange={this.valueChangeHandler}
      />
    );
  };

  ifActive = (name: string, component: any) => {
    if (this.state.activeFields.includes(name)) {
      return null;
    } else {
      return component;
    }
  };
}

export default withTranslation("plugins")(LdapConfigurationForm);
