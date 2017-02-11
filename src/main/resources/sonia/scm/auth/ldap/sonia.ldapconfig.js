/*
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
Ext.ns("Sonia.ldap");

Sonia.ldap.ConfigPanel = Ext.extend(Sonia.config.ConfigForm, {
  
  profiles: [{
    name: 'ActiveDirectory',
    fields: {
      'attribute-name-id': 'sAMAccountName',
      'attribute-name-fullname': 'cn',
      'attribute-name-mail': 'mail',
      'attribute-name-group': 'memberOf',
      'search-filter': '(&(objectClass=Person)(sAMAccountName={0}))',
      'search-filter-group': '(&(objectClass=group)(member={0}))',
      'search-scope': 'sub',
      'unit-people': '',
      'unit-groups': '',
      'referral-strategy': 'FOLLOW'
    }
  },{
    name: 'Apache Directory Server',
    fields: {
      'attribute-name-id': 'cn',
      'attribute-name-fullname': 'displayName',
      'attribute-name-mail': 'mail',
      'attribute-name-group': 'memberOf',
      'search-filter': '(&(objectClass=inetOrgPerson)(cn={0}))',
      'search-filter-group': '(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))',
      'search-scope': 'sub',
      'unit-people': 'ou=People',
      'unit-groups': 'ou=Groups',
      'enable-nested-ad-groups': 'false',
      'referral-strategy': 'FOLLOW'
    }
  },{
    name: 'OpenDS/OpenDJ',
    fields: {
      'attribute-name-id': 'uid',
      'attribute-name-fullname': 'cn',
      'attribute-name-mail': 'mail',
      'attribute-name-group': 'memberOf',
      'search-filter': '(&(objectClass=inetOrgPerson)(uid={0}))',
      'search-filter-group': '(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))',
      'search-scope': 'sub',
      'unit-people': 'ou=People',
      'unit-groups': 'ou=Groups',
      'referral-strategy': 'FOLLOW',
      'enable-nested-ad-groups': 'false'
    }
  },{
    name: 'OpenLDAP',
    fields: {
      'attribute-name-id': 'uid',
      'attribute-name-fullname': 'cn',
      'attribute-name-mail': 'mail',
      'attribute-name-group': 'memberOf',
      'search-filter': '(&(objectClass=inetOrgPerson)(uid={0}))',
      'search-filter-group': '(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))',
      'search-scope': 'sub',
      'unit-people': 'ou=People',
      'unit-groups': 'ou=Groups',
      'referral-strategy': 'FOLLOW',
      'enable-nested-ad-groups': 'false'
    }
  },{
    name: 'OpenLDAP (Posix)',
    fields: {
      'attribute-name-id': 'uid',
      'attribute-name-fullname': 'cn',
      'attribute-name-mail': 'mail',
      'attribute-name-group': 'memberOf',
      'search-filter': '(&(objectClass=posixAccount)(uid={0}))',
      'search-filter-group': '(&(objectClass=posixGroup)(memberUid={1}))',
      'search-scope': 'sub',
      'unit-people': 'ou=People',
      'unit-groups': 'ou=Groups',
      'referral-strategy': 'FOLLOW',
      'enable-nested-ad-groups': 'false'
    }
  },{
    name: 'Sun/Oracle Directory Server',
    fields: {
      'attribute-name-id': 'uid',
      'attribute-name-fullname': 'cn',
      'attribute-name-mail': 'mail',
      'attribute-name-group': 'memberOf',
      'search-filter': '(&(objectClass=inetOrgPerson)(uid={0}))',
      'search-filter-group': '(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))',
      'search-scope': 'sub',
      'unit-people': 'ou=People',
      'unit-groups': 'ou=Groups',
      'referral-strategy': 'FOLLOW',
      'enable-nested-ad-groups': 'false'
    }
  },{
    name: 'Custom',
    fields: {}
  }],

  // labels
  profileText: 'Profile',
  
  titleText: 'LDAP Authentication',
  uniqueIdAttributeText: 'Unique LDAP identifier',
  idAttributeText: 'ID Attribute Name',
  fullnameAttributeText: 'Fullname Attribute Name',
  mailAttributeText: 'Mail Attribute Name',
  groupAttributeText: 'Group Attribute Name',
  baseDNText: 'Base DN',
  connectionDNText: 'Connection DN',
  connectionPasswordText: 'Connection Password',
  hostURLText: 'Host URL',
  searchFilterText: 'Search Filter',
  searchFilterGroupText: 'Group Search Filter',
  searchScopeText: 'Search Scope',
  groupsUnitText: 'Groups Unit',
  peopleUnitText: 'People Unit',
  referralStrategyText: 'Referral Strategy',
  enableNestedADGroupsText: 'Enable nested ad groups',
  enableStartTlsText: 'Use StartTLS',
  enabledText: 'Enabled',
  testConnectionText: 'Test Connection',
  
  // help texts
  profileHelpText: 'Predifined profiles for different LDAP-Servers.',

  uniqueIdAttributeHelpText: 'Unique identifier for the LDAP configuration.',
  idAttributeHelpText: 'LDAP attribute name holding the username (e.g. uid).',
  fullnameAttributeHelpText: 'LDAP attribute name for the users displayname (e.g. cn).',
  mailAttributeHelpText: 'LDAP attribute name for the users e-mail address (e.g. mail).',
  // TODO improve
  groupAttributeHelpText: 'The name of the ldap attribute which contains the group names of the user.',
  baseDNHelpText: 'The basedn for example: dc=example,dc=com',
  connectionDNHelpText: 'The complete dn of the proxy user. <strong>Note:<strong> \n\
                         This user needs read an search privileges for the id, mail and fullname attributes.',
  connectionPasswordHelpText: 'The password for proxy user.',
  hostURLHelpText: 'The url for the ldap server. For example: ldap://localhost:389/',
  searchFilterHelpText: 'The search filter to find the users. <strong>Note:</strong>\n\
                        {0} will be replaced by the username.',
  searchFilterGroupHelpText: 'The search filter to find groups of the user.<br /><b>Note:</b><br />\n\
                        {0} will be replaced by the dn of the user.<br />\n\
                        {1} will be replaced by the username.<br />\n\
                        {2} will be replaced by the email address of the user.',
  searchScopeHelpText: 'The scope for the user search.',
  peopleUnitHelpText: 'The relative location of the users. For example: ou=People',
  groupsUnitHelpText: 'The relative location of the groups. For example: ou=Groups',
  referralStrategyHelpText: 'Strategy to handle ldap referrals.<br />\n\
                            <b>IGNORE:</b> will ignore all referrals.<br />\n\
                            <b>FOLLOW</b> automatically follow any referrals.<br />\n\
                            <b>THROW</b> throw a ReferralException for each referral.',
  enableNestedADGroupsHelpText: 'Enable search for nested ActiveDirectory groups. <b>Note:</b> Nested ad groups work only for ActiveDirectory.',
  enableStartTlsHelpText: 'Use StartTLS extension to encrypt the connection to the directory server.',
  enabledHelpText: 'Enables or disables the ldap authentication.',
  
  // errors 
  errorBoxTitle: 'Error',
  errorOnSubmitText: 'Error during config submit.',
  errorOnLoadText: 'Error during config load.',
  
  initComponent: function(){
    
    var config = {
      title : this.titleText,
      items : [{
        xtype : 'textfield',
        fieldLabel : this.uniqueIdAttributeText,
        name : 'unique-id',
        allowBlank : false,
        helpText: this.uniqueIdAttributeHelpText,
        listeners: {
            scope : this,
            change: function(textfield, newValue, oldValue){
            this.updateTitleText();
            }
        }
      },{
        xtype : 'combo',
        fieldLabel : this.profileText,
        name : 'profile',
        allowBlank : true,
        helpText: this.profileHelpText,
        valueField: 'name',
        displayField: 'name',
        typeAhead: false,
        editable: false,
        triggerAction: 'all',
        mode: 'local',
        store: new Ext.data.JsonStore({
          idProperty: 'name',
          fields: ['name', 'fields'],
          data: this.profiles
        }),
        listeners: {
          select: {
            fn: this.changeProfile,
            scope: this
          }
        }
      },{
        xtype : 'textfield',
        fieldLabel : this.idAttributeText,
        name : 'attribute-name-id',
        allowBlank : false,
        helpText: this.idAttributeHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.fullnameAttributeText,
        name : 'attribute-name-fullname',
        allowBlank : false,
        helpText: this.fullnameAttributeHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.mailAttributeText,
        name : 'attribute-name-mail',
        allowBlank : false,
        helpText: this.mailAttributeHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.groupAttributeText,
        name : 'attribute-name-group',
        allowBlank : true,
        helpText: this.groupAttributeHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.baseDNText,
        name : 'base-dn',
        allowBlank : true,
        helpText: this.baseDNHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.connectionDNText,
        name : 'connection-dn',
        allowBlank : true,
        helpText: this.connectionDNHelpText
      },{
        xtype : 'textfield',
        inputType: 'password',
        fieldLabel : this.connectionPasswordText,
        name : 'connection-password',
        allowBlank : true,
        helpText: this.connectionPasswordHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.hostURLText,
        name : 'host-url',
        allowBlank : false,
        helpText: this.hostURLHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.searchFilterText,
        name : 'search-filter',
        allowBlank : false,
        helpText: this.searchFilterHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.searchFilterGroupText,
        name : 'search-filter-group',
        allowBlank : true,
        helpText: this.searchFilterGroupHelpText        
      },{
        xtype : 'combo',
        fieldLabel : this.searchScopeText,
        name : 'search-scope',
        allowBlank : false,
        helpText: this.searchScopeHelpText,
        valueField: 'scope',
        displayField: 'scope',
        typeAhead: false,
        editable: false,
        triggerAction: 'all',
        mode: 'local',
        store: new Ext.data.SimpleStore({
          fields: ['scope'],
          data: [
            ['object'],
            ['one'],
            ['sub']
          ]
        })
      },{
        xtype : 'textfield',
        fieldLabel : this.peopleUnitText,
        name : 'unit-people',
        allowBlank : true,
        helpText: this.peopleUnitHelpText
      },{
        xtype : 'textfield',
        fieldLabel : this.groupsUnitText,
        name : 'unit-groups',
        allowBlank : true,
        helpText: this.groupsUnitHelpText
      },{
        xtype : 'combo',
        name: 'referral-strategy',
        fieldLabel: this.referralStrategyText,
        helpText: this.referralStrategyHelpText,
        valueField: 'rs',
        displayField: 'rs',
        typeAhead: false,
        editable: false,
        triggerAction: 'all',
        mode: 'local',
        store: new Ext.data.SimpleStore({
          fields: ['rs'],
          data: [
            ['FOLLOW'],
            ['IGNORE'],
            ['THROW']
          ]
        })
      },{
        xtype: 'checkbox',
        fieldLabel : this.enableNestedADGroupsText,
        name: 'enable-nested-ad-groups',
        inputValue: 'true',
        helpText: this.enableNestedADGroupsHelpText
      },{
        xtype: 'checkbox',
        fieldLabel : this.enableStartTlsText,
        name: 'enable-starttls',
        inputValue: 'true',
        helpText: this.enableStartTlsHelpText        
      },{
        xtype: 'checkbox',
        fieldLabel : this.enabledText,
        name: 'enabled',
        inputValue: 'true',
        helpText: this.enabledHelpText
      },{
        xtype: 'button',
        fieldLabel : this.testConnectionText,
        name: 'testConnection',
        text: this.testConnectionText,
        scope: this,
        handler: this.testConnection
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.ldap.ConfigPanel.superclass.initComponent.apply(this, arguments);
  },

  updateTitleText: function() {
    this.items.items[0].setTitle(this.find("name", "unique-id")[0].getValue() + ' ' + this.titleText);
  },

  testConnection: function(){
    var window = new Sonia.ldap.ConnectionTestWindow({
      data: this.getForm().getValues()
    });
    window.show();
  },
  
  changeProfile: function(combo, record, number){
    var profile = record.get('name');
    if (debug){
      console.debug( 'select profile "' + profile + '"');
    }

    var fields = record.get('fields');
    if (fields){
      this.applyProfileFields(fields);
    }
  },
  
  applyProfile: function(profile){
    for ( var i=0; i<this.profiles.length; i++ ){
      if ( this.profiles[i].name == profile ){
        this.toggleFields(this.profiles[i].fields);
        break;
      }
    }
  },
  
  applyProfileFields: function(fields){
    this.toggleFields(fields);
    this.getForm().setValues(fields);
  },
  
  toggleFields: function(profileFields){
    var form = this.getForm();
    form.items.each( function(field){
      var visible = profileFields[field.getName()] == null;
      field.setVisible(visible);
    }, this);    
  },
  
  onLoad: function(configurationData){
    this.load(configurationData);
    if ( configurationData.profile != 'Custom' ){
      this.applyProfile(configurationData.profile);
    }
    this.updateTitleText();
  }
  
});

// register xtype
Ext.reg("ldapConfigPanel", Sonia.ldap.ConfigPanel);


// i18n

if ( i18n != null && i18n.country == 'de' ){

  Ext.override(Sonia.ldap.ConfigPanel, {

    titleText: 'LDAP Atthentifizierung',
    idAttributeText: 'Attributname: ID',
    fullnameAttributeText: 'Atributname: Vollständiger Name',
    mailAttributeText: 'Attributname: eMail-Adresse',
    groupAttributeText: 'Attributname: Gruppen',
    baseDNText: 'Base DN',
    connectionDNText: 'Verbindungs-DN',
    connectionPasswordText: 'Verbindungs-Password',
    hostURLText: 'Server URL',
    searchFilterText: 'Suchfilter',
    searchFilterGroupText: 'Gruppensuchfilter',
    searchScopeText: 'Suchtiefe (scope)',
    groupsUnitText: 'Gruppen (ou)',
    peopleUnitText: 'Personen (ou)',
    referralStrategyText: 'Referenz Strategie',
    enableNestedADGroupsText: 'Aktiviere verschachtelte AD Gruppen',
    enableStartTlsText: 'Verwende StartTLS',
    enabledText: 'Aktiviert',
  
    // help texts
    profileHelpText: 'Vordefinierte LDAP-Profile.',
    idAttributeHelpText: 'LDAP Attributname der eindeutigen ID der Accounts (z.B. uid)',
    fullnameAttributeHelpText: 'LDAP Attributname des vollständigen Accountnames (z.B. cn)',
    mailAttributeHelpText: 'LDAP Attributname der Account-eMail-Adresse (z.B. mail)',

    groupAttributeHelpText: 'Name des LDAP Grupen-Attributes (z.B. group)',
    baseDNHelpText: 'Base DN zum Beispiel: dc=example,dc=com',
    connectionDNHelpText: 'Vollständige DN des Proxy-Account <strong>Achtung<strong> \n\
                         Dieser Account benötigt lese und such Berechtigung für die id, mail und fullname Attribute.',
    connectionPasswordHelpText: 'Das Passwort des Proxy-Account',
    hostURLHelpText: 'URL zum LDAP-Server (z.B. ldap://localhost:389/)',
    searchFilterHelpText: 'Personensuchfilter <strong>Achtung:</strong>\n\
                        {0} wird durch den Nutzernamen ersetzt.',
    searchFilterGroupHelpText: 'Gruppensuchfilter. <b>Achtung:</b>\n\
                        {0} wird durch die DN des Benutzers erssetzt.<br />\n\
                        {1} wird durch den Nutzernamen ersetzt.<br />\n\
                        {2} wird durch die E-Mail des Benutzers ersetzt.',
    searchScopeHelpText: 'Suchtiefe (scope) für die Personensuche',
    peopleUnitHelpText: 'Relativer Personen-Pfad (z.B. ou=People)',
    groupsUnitHelpText: 'Relativer Gruppen-Pfad (z.B. ou=Groups)',
    referralStrategyHelpText: 'Strategie wie Ldap-Referenzen behandelt werden.<br />\n\
                            <b>IGNORE:</b> alle Referenzen werden ignoriert.<br />\n\
                            <b>FOLLOW</b> alle Referenzen werden verfolgt.<br />\n\
                            <b>THROW</b> wirft eine ReferralException für jede Referenz.',
    enableNestedADGroupsHelpText: 'Aktiviert / Deaktiviert verschachtelte ActiveDirectory Gruppen. \n\
                        <b>Achtung:</b> Verschachtelte Gruppen funktionieren nur mit einem ActiveDirectory Server.',
    enableStartTlsHelpText: 'Verwende StartTLS Erweiterung um eine verschlüsselte Verbindung zum DirecotryServer aufzubauen.',
    enabledHelpText: 'Aktiviert / Deaktiviert die LDAP Authentifizierung',
    
    // errors 
    errorBoxTitle: 'Fehler',
    errorOnSubmitText: 'Fehler beim speichern der Konfiguration.',
    errorOnLoadText: 'Fehler beim laden der Konfiguration.'

  });

}
