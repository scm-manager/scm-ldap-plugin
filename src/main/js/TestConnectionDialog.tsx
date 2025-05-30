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

import React from "react";
import { apiClient, Button, InputField, Modal, Subtitle, Tag } from "@scm-manager/ui-components";
import { withTranslation, WithTranslation } from "react-i18next";

type TestResultUser = {
  valid: boolean;
  name: string;
  displayName: string;
  mailAddress?: string;
};

type TestResult = {
  configured: boolean;
  connected: boolean;
  userFound: boolean;
  userAuthenticated: boolean;
  exception: string;
  user: TestResultUser;
  groups: string[];
};

type Props = WithTranslation & {
  config: any;
  testLink: string;
  onClose: () => any;
};

type State = {
  username: string;
  password: string;
  testResult?: TestResult;
};

class TestConnectionDialog extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      username: "",
      password: "",
      testResult: undefined
    };
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
        <Button label={t("scm-ldap-plugin.testForm.submit")} disabled={!valid} action={this.onTest} color="primary" />
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

    const success = <Tag color="success" label="Success" />;
    const failure = <Tag color="danger" label="Failure" />;
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
                {t("scm-ldap-plugin.testForm.result.userDetailsName")}: {testResult.user.name}
              </li>
              <li>
                {t("scm-ldap-plugin.testForm.result.userDetailsDisplayName")}: {testResult.user.displayName}
              </li>

              <li>
                {t("scm-ldap-plugin.testForm.result.userDetailsMail")}:{" "}
                {testResult?.user?.mailAddress
                  ? testResult.user.mailAddress
                  : t("scm-ldap-plugin.testForm.result.missingValidMail")}
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
      <>
        <hr />
        <Subtitle subtitle={t("scm-ldap-plugin.testForm.result.header")} />
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
      </>
    );
  };

  usernameChanged = (value: string) => {
    this.setState({
      username: value
    });
  };

  passwordChanged = (value: string) => {
    this.setState({
      password: value
    });
  };

  onTest = () => {
    apiClient
      .post(this.props.testLink, {
        username: this.state.username,
        password: this.state.password,
        config: this.props.config
      })
      .then(result => result.json())
      .then(body =>
        this.setState({
          testResult: body
        })
      )
      .catch(error =>
        this.setState({
          testResult: {
            exception: error
          }
        })
      );
  };
}

export default withTranslation("plugins")(TestConnectionDialog);
