// @flow

import React from "react";
import {
  apiClient,
  Button,
  InputField,
  Modal
} from "@scm-manager/ui-components";
import { translate } from "react-i18next";

type TestResultUser = {
  valid: boolean,
  name: string,
  displayName: string,
  mailAddress: string
};

type TestResult = {
  configured: boolean,
  connected: boolean,
  userFound: boolean,
  userAuthenticated: boolean,
  exception: string,
  user: TestResultUser,
  groups: string[]
};

type Props = {
  config: any,
  testLink: string,
  onClose: () => any,
  // context props
  t: string => string
};

type State = {
  username: string,
  password: string,
  testResult: TestResult
};

class TestConnectionDialog extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = { username: "", password: "", testResult: undefined };
  }

  render() {
    const { t, onClose } = this.props;
    const { username, password } = this.state;
    const valid = username !== "" && password !== "";

    const body = (
      <>
        <div className="columns">
          <div className="column">
            <InputField
              name="username"
              label={t("scm-ldap-plugin.testForm.username")}
              value={username}
              onReturnPressed={this.onTest}
              onChange={this.usernameChanged}
            />
          </div>
          <div className="column">
            <InputField
              name="password"
              label={t("scm-ldap-plugin.testForm.password")}
              value={password}
              type="password"
              onReturnPressed={this.onTest}
              onChange={this.passwordChanged}
            />
          </div>
        </div>
        {this.renderTestResult()}
      </>
    );
    const footer = (
      <>
        <Button
          label={t("scm-ldap-plugin.testForm.submit")}
          disabled={!valid}
          action={this.onTest}
          color="primary"
        />
        <Button label={t("scm-ldap-plugin.testForm.abort")} action={onClose} />
      </>
    );

    return (
      <Modal
        title={t("scm-ldap-plugin.testForm.title")}
        closeFunction={() => onClose()}
        body={body}
        footer={footer}
        active={true}
      />
    );
  }

  renderTestResult = () => {
    const { t } = this.props;
    const { testResult } = this.state;

    if (!testResult) {
      return null;
    }

    const success = <span className="tag is-success">Success</span>;
    const failure = <span className="tag is-danger">Failure</span>;
    const successOrFailure = (r: boolean) => (r ? success : failure);

    const testResultDetailRows = testResult.user ? (
      <>
        <tr>
          <td>{t("scm-ldap-plugin.testForm.result.userValid")}</td>
          <td>{successOrFailure(testResult.user && testResult.user.valid)}</td>
        </tr>
        <tr>
          <td>{t("scm-ldap-plugin.testForm.result.userDetails")}</td>
          <td>
            <ul>
              <li>
                {t("scm-ldap-plugin.testForm.result.userDetailsName")}:{" "}
                {testResult.user.name}
              </li>
              <li>
                {t("scm-ldap-plugin.testForm.result.userDetailsDisplayName")}:{" "}
                {testResult.user.displayName}
              </li>
              <li>
                {t("scm-ldap-plugin.testForm.result.userDetailsMail")}:{" "}
                {testResult.user.mailAddress}
              </li>
            </ul>
          </td>
        </tr>
        <tr>
          <td>{t("scm-ldap-plugin.testForm.result.groups")}</td>
          <td>{testResult.groups.join(", ")}</td>
        </tr>
      </>
    ) : (
      <>
        <tr>
          <td>{t("scm-ldap-plugin.testForm.result.exception")}</td>
          <td>{testResult.exception}</td>
        </tr>
      </>
    );
    return (
      <section className="section">
        <h1 className="title">{t("scm-ldap-plugin.testForm.result.header")}</h1>
        <table className="table">
          <tr>
            <td>{t("scm-ldap-plugin.testForm.result.configured")}</td>
            <td>{successOrFailure(testResult.configured)}</td>
          </tr>
          <tr>
            <td>{t("scm-ldap-plugin.testForm.result.connected")}</td>
            <td>{successOrFailure(testResult.connected)}</td>
          </tr>
          <tr>
            <td>{t("scm-ldap-plugin.testForm.result.userFound")}</td>
            <td>{successOrFailure(testResult.userFound)}</td>
          </tr>
          <tr>
            <td>{t("scm-ldap-plugin.testForm.result.userAuthenticated")}</td>
            <td>{successOrFailure(testResult.userAuthenticated)}</td>
          </tr>
          {testResultDetailRows}
        </table>
      </section>
    );
  };

  usernameChanged = (value: string) => {
    this.setState({ username: value });
  };

  passwordChanged = (value: string) => {
    this.setState({ password: value });
  };

  onTest = () => {
    apiClient
      .post(this.props.testLink, {
        username: this.state.username,
        password: this.state.password,
        config: this.props.config
      })
      .then(result => result.json())
      .then(body => this.setState({ testResult: body }))
      .catch(error => this.setState({ testResult: { exception: error } }));
  };
}

export default translate("plugins")(TestConnectionDialog);
