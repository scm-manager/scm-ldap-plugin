/* *
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
      'search-filter': '(&(objectClass=Person)(uid={0}))',
      'search-filter-group': '(&(objectClass=group)(member={0}))',
      'search-scope': 'sub',
      'unit-people': 'ou=Users',
      'unit-groups': 'ou=Groups'
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
      'unit-groups': 'ou=Groups'
    }
  },{
    name: 'OpenDS',
    fields: {
      'attribute-name-id': 'uid',
      'attribute-name-fullname': 'cn',
      'attribute-name-mail': 'mail',
      'attribute-name-group': 'memberOf',
      'search-filter': '(&(objectClass=inetOrgPerson)(uid={0}))',
      'search-filter-group': '(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))',
      'search-scope': 'sub',
      'unit-people': 'ou=People',
      'unit-groups': 'ou=Groups'
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
      'unit-groups': 'ou=Groups'
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
      'unit-groups': 'ou=Groups'
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
      'unit-groups': 'ou=Groups'
    }
  },{
    name: 'Custom'
  }],
  
  customFields: [      
    'attribute-name-id',
    'attribute-name-fullname',
    'attribute-name-mail',
    'attribute-name-group',
    'search-filter',
    'search-filter-group',
    'search-scope',
    'unit-people',
    'unit-groups'
  ],

  // labels
  profileText: 'Profile',
  
  titleText: 'LDAP Authentication',
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
  enabledText: 'Enabled',
  
  // help texts
  profileHelpText: 'Predifined profiles for LDAP-Server',
  
  idAttributeHelpText: 'LDAP attribute name holding the username (e.g. uid)',
  fullnameAttributeHelpText: 'LDAP attribute name for the users displayname (e.g. cn)',
  mailAttributeHelpText: 'LDAP attribute name for the users e-mail address (e.g. mail)',
  // TODO improve
  groupAttributeHelpText: 'The name of the ldap attribute which contains the group names of the user',
  baseDNHelpText: 'The basedn for example: dc=example,dc=com',
  connectionDNHelpText: 'The complete dn of the proxy user. <strong>Note:<strong> \n\
                         This user needs read an search privileges for the id, mail and fullname attributes.',
  connectionPasswordHelpText: 'The password for proxy user.',
  hostURLHelpText: 'The url for the ldap server. For example: ldap://localhost:389/',
  searchFilterHelpText: 'The search filter to find the users. <strong>Note:</strong>\n\
                        {0} will be replaced by the username.',
  searchFilterGroupHelpText: 'The search filter to find groups of the user. <string>Note:</strong>\n\
                        {0} will be replaced by the dn of the user.<br />\n\
                        {1} will be replaced by the username.<br />\n\
                        {2} will be replaced by the email address of the user.',
  searchScopeHelpText: 'The scope for the user search.',
  peopleUnitHelpText: 'The relative location of the users. For example: ou=People',
  groupsUnitHelpText: 'The relative location of the groups. For example: ou=Groups',
  enabledHelpText: 'Enables or disables the ldap authentication',
  
  // errors 
  errorBoxTitle: 'Error',
  errorOnSubmitText: 'Error during config submit.',
  errorOnLoadText: 'Error during config load.',
  
  initComponent: function(){
    
    var config = {
      title : this.titleText,
      items : [{
        xtype : 'combo',
        fieldLabel : this.profileText,
        name : 'profile',
        allowBlank : true,
        helpText: this.searchScopeHelpText,
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
        xtype: 'checkbox',
        fieldLabel : this.enabledText,
        name: 'enabled',
        inputValue: 'true',
        helpText: this.enabledHelpText
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.ldap.ConfigPanel.superclass.initComponent.apply(this, arguments);
  },
  
  changeProfile: function(combo, record, number){
    var profile = record.get('name');
    if (debug){
      console.debug( 'select profile "' + profile + '"');
    }
    
    if ( profile == 'Custom' ){
      this.toggleFields(true);
    } else {
      var fields = record.get('fields');
      if (fields){
        this.toggleFields(false);
        this.getForm().setValues(fields);
      }
    }
  },
  
  toggleFields: function(visible){
    var form = this.getForm();
    Ext.each(this.customFields, function(field){
      form.findField(field).setVisible(visible);
    }, this);    
  },
  
  onSubmit: function(values){
    this.el.mask(this.submitText);
    Ext.Ajax.request({
      url: restUrl + 'config/auth/ldap.json',
      method: 'POST',
      jsonData: values,
      scope: this,
      disableCaching: true,
      success: function(response){
        this.el.unmask();
      },
      failure: function(){
        Ext.MessageBox.show({
          title: this.errorBoxTitle,
          msg: this.errorOnSubmitText,
          buttons: Ext.MessageBox.OK,
          icon:Ext.MessageBox.ERROR
        });
      }
    });
  },

  onLoad: function(el){
    var tid = setTimeout( function(){
      el.mask(this.loadingText);
    }, 100);
    Ext.Ajax.request({
      url: restUrl + 'config/auth/ldap.json',
      method: 'GET',
      scope: this,
      disableCaching: true,
      success: function(response){
        var obj = Ext.decode(response.responseText);
        this.load(obj);
        if ( obj.profile != 'Custom' ){
          this.toggleFields(false);
        }
        clearTimeout(tid);
        el.unmask();
      },
      failure: function(){
        el.unmask();
        clearTimeout(tid);
        Ext.MessageBox.show({
          title: this.errorBoxTitle,
          msg: this.errorOnLoadText,
          buttons: Ext.MessageBox.OK,
          icon:Ext.MessageBox.ERROR
        });
      }
    });
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
    enabledText: 'Aktiviert',
  
    // help texts
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
    searchFilterGroupHelpText: 'Gruppensuchfilter. <string>Achtung:</strong>\n\
                        {0} wird durch die DN des Benutzers erssetzt.<br />\n\
                        {1} wird durch den Nutzernamen ersetzt.<br />\n\
                        {2} wird durch die E-Mail des Benutzers ersetzt.',
    searchScopeHelpText: 'Suchtiefe (scope) für die Personensuche',
    peopleUnitHelpText: 'Relativer Personen-Pfad (z.B. ou=People)',
    groupsUnitHelpText: 'Relativer Gruppen-Pfad (z.B. ou=Groups)',
    enabledHelpText: 'Aktiviert / Deaktiviert die LDAP Authentifizierung',
    
    // errors 
    errorBoxTitle: 'Fehler',
    errorOnSubmitText: 'Fehler beim speichern der Konfiguration.',
    errorOnLoadText: 'Fehler beim laden der Konfiguration.'

  });

}

// regist config panel
registerGeneralConfigPanel({
  id: 'ldapConfigPanel',
  xtype: 'ldapConfigPanel'
});
