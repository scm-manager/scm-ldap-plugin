// @flow
import React from "react";
import {Title, Configuration} from "@scm-manager/ui-components"
import LdapConfigurationForm from "./LdapConfigurationForm";
import {translate} from "react-i18next";

type Props = {
  link: string,
  t: (string) => string
}

class LdapConfiguration extends React.Component<Props> {

  render(): React.ReactNode {
    const {t, link} = this.props;
    return (
      <>
        <Title title={t("scm-ldap-plugin.form.header")}></Title>
        <Configuration link={link} t={t} render={props => <LdapConfigurationForm {...props}/>}/>
      </>
    );
  }
}

export default translate("plugins")(LdapConfiguration);
