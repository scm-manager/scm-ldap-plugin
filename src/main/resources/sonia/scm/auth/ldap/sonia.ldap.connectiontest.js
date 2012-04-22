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

Sonia.ldap.ConnectionTestForm = Ext.extend(Ext.FormPanel,{
  
  usernameText: 'Username',
  passwordText: 'Password',
  testText: 'Test',
  cancelText: 'Cancel',
  waitTitleText: 'Connecting',
  waitMsgText: 'Sending data...',
  failedMsgText: 'LDAP Connection Test failed!',

  initComponent: function(){

    var config = {
      labelWidth: 80,
      url: restUrl + "config/auth/ldap/test.json",
      frame: false,
      defaultType: 'textfield',
      monitorValid: true,
      bodyCssClass: 'x-panel-mc',
      bodyStyle: 'padding: 5px',
      listeners: {
        afterrender: function(){
          Ext.getCmp('ldapTestUsername').focus(true, 500);
        }
      },
      items:[{
        id: 'ldapTestUsername',
        fieldLabel: this.usernameText,
        name: 'username',
        allowBlank:false,
        listeners: {
          specialkey: {
            fn: this.specialKeyPressed,
            scope: this
          }
        }
      },{
        fieldLabel: this.passwordText,
        name: 'password',
        inputType: 'password',
        allowBlank: false,
        listeners: {
          specialkey: {
            fn: this.specialKeyPressed,
            scope: this
          }
        }
      },{
        id: 'ldapTestResultPanel',
        xtype: 'panel',
        bodyCssClass: 'x-panel-mc',
        tpl: new Ext.XTemplate([
          '<p>',
          '  Connection: {bind}<br />',
          '  Search user: {searchUser}<br />',
          '  Authenticate user: {authenticateUser}<br />',
          '  <tpl if="exception">',
          '    Exception: {exception}<br />',
          '  </tpl>',
          '  <tpl if="user">',
          '    <p>&nbsp;</p>',
          '    User:<br />',
          '    <tpl for="user">',
          '      - Name: {name}<br />',
          '      - Display Name: {displayName}<br />',
          '      - Mail: {mail}<br />',
          '    </tpl>',
          '  </tpl>',
          '  <tpl if="groups">',
          '    <p>&nbsp;</p>',
          '    Groups<br />',
          '    <tpl for="groups">',
          '      - {.}<br />',
          '    </tpl>',
          '  </tpl>',
          '</p>'
        ])
      }],
      buttons:[{
        text: this.cancelText,
        scope: this,
        handler: this.cancel
      },{
        text: this.testText,
        formBind: true,
        scope: this,
        handler: this.testConnection
      }]
    };

    this.addEvents('cancel');

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.ldap.ConnectionTestForm.superclass.initComponent.apply(this, arguments);
  },

  cancel: function(){
    this.fireEvent('cancel');
  },
  
  getBooleanLabel: function(value){
    var label = null;
    if (value){
      label = '<span style="color: green">SUCCESS</span>';
    } else {
      label = '<span style="color: red">FAILURE</span>';
    }
    return label;
  },
  
  showResult: function(data){
    if (debug){
      console.debug('connection test results:');
      console.debug(data);
    }
    data.bind = this.getBooleanLabel(data.bind);
    data.searchUser = this.getBooleanLabel(data.searchUser);
    data.authenticateUser = this.getBooleanLabel(data.authenticateUser);
    
    var resultPanel = Ext.getCmp('ldapTestResultPanel');
    resultPanel.tpl.overwrite(resultPanel.body, data);
  },

  testConnection: function(){
    var form = this.getForm();
    form.submit({
      scope: this,
      method: 'POST',
      waitTitle: this.waitTitleText,
      waitMsg: this.waitMsgText,

      success: function(form, action){
        if ( debug ){
          console.debug( 'test connection success' );
        }
        this.showResult(action.result);
      },

      failure: function(form){
        if ( debug ){
          console.debug( 'test connection failed' );
        }
        Ext.Msg.alert(this.failedMsgText);
        form.reset();
      }
    });
  },

  specialKeyPressed: function(field, e){
    if (e.getKey() == e.ENTER) {
      var form = this.getForm();
      if ( form.isValid() ){
        this.testConnection();
      }
    }
  }

});

Sonia.ldap.ConnectionTestWindow = Ext.extend(Ext.Window,{

  titleText: 'LDAP Connection Test',

  initComponent: function(){
    var form = new Sonia.ldap.ConnectionTestForm();
    form.on('cancel', function(){
      this.close();
    }, this);

    var config = {
      layout:'fit',
      width: 480,
      height: 320,
      closable: true,
      resizable: true,
      plain: true,
      border: false,
      modal: true,
      title: this.titleText,
      items: [form]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.ldap.ConnectionTestWindow.superclass.initComponent.apply(this, arguments);
  }

});
