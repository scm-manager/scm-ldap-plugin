// @flow

import React from "react";
import {apiClient, Button, InputField, Modal, SubmitButton} from "@scm-manager/ui-components";
import {translate} from "react-i18next";

type Props = {
  config: any,
  testLink: string,
  onClose: () => any,
  // context props
  t: string => string
};

type State = {
  username: string,
  password: string
};

class TestConnectionDialog extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = { username: "", password: "" };
  }

  render() {
    const { t, onClose } = this.props;
    const { username, password } = this.state;
    const valid = username !== "" && password !== "";

    const body = (
      <>
        <InputField name="username"
                    label={t("scm-ldap-plugin.testForm.username")}
                    value={username}
                    onChange={this.usernameChanged}/>
        <InputField name="password"
                    label={t("scm-ldap-plugin.testForm.password")}
                    value={password}
                    type="password"
                    onChange={this.passwordChanged}/>
        <Button
          label={t("scm-ldap-plugin.testForm.submit")}
          disabled={!valid}
          action={this.onTest}
        />
        <Button
          label={t("scm-ldap-plugin.testForm.abort")}
          action={onClose}
        />
      </>
    );

    return (
      <Modal
        title={t("scm-ldap-plugin.testForm.title")}
        closeFunction={() => onClose()}
        body={body}
        active={true}
      />
    );
  }

  usernameChanged = (value: string) => {
    this.setState({ username: value });
  };

  passwordChanged = (value: string) => {
    this.setState({ password: value });
  };

  onTest = () => {
    apiClient.post(this.props.testLink, {username: this.state.username, password: this.state.password, config: this.props.config})
      .then(result => console.log(result))
      .catch(error => console.log(error));
  };
}

export default translate("plugins")(TestConnectionDialog);
