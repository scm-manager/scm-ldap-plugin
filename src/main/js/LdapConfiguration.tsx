import React from "react";
import { Title, Configuration } from "@scm-manager/ui-components";
import LdapConfigurationForm from "./LdapConfigurationForm";
import { withTranslation, WithTranslation } from "react-i18next";

type Props = WithTranslation & {
  link: string;
};

class LdapConfiguration extends React.Component<Props> {
  render(): React.ReactNode {
    const { t, link } = this.props;
    return (
      <>
        <Title title={t("scm-ldap-plugin.form.header")} />
        <Configuration link={link} t={t} render={props => <LdapConfigurationForm {...props} />} />
      </>
    );
  }
}

export default withTranslation("plugins")(LdapConfiguration);
