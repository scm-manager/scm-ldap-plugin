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

Sonia.ldap.MainConfigPanel = Ext.extend(Sonia.config.ConfigPanel, {

  titleText: 'LDAP Authentication',
  addNewConfigText: 'Add New Config',
  saveText: 'Save',
  // errors 
  errorBoxTitle: 'Error',
  errorOnSubmitText: 'Error during config submit.',
  errorOnLoadText: 'Error during config load.',
  
  configPanels: [],

  initComponent: function(){
    
    var config = {
      title : this.titleText,
      buttonAlign: 'left',
      listeners: {
        render: function(){
          if ( this.onLoad && Ext.isFunction( this.onLoad ) ){
            this.onLoad(this.el);
          }
        },
        scope: this
      },      
      buttons: [{
        xtype: 'button',
        fieldLabel : this.addNewConfigText,
        name: 'addNewConfig',
        text: this.addNewConfigText,
        scope: this,
        handler: this.addNewConfigHandler
      },{
        xtype: 'button',
        fieldLabel : this.saveText,
        name: 'save',
        text: this.saveText,
        scope: this,
        handler: this.onSubmit
      }]
    }
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.ldap.MainConfigPanel.superclass.initComponent.apply(this, arguments);
  },
  
  addNewConfigHandler: function() {
      this.addNewConfig();
  },
  
  addNewConfig: function(values){
      var configPanel = new Sonia.ldap.ConfigPanel();
      
      // Get the save button and hide it.
      var saveButton = configPanel.items.items[0].fbar.find('text', configPanel.saveButtonText)[0];
      if (saveButton) {
          saveButton.hide();
      }
      
      var deleteButton = new Ext.Button({
          id:Ext.id(),
          text: 'Delete',
          handler: function(){
            this.remove(configPanel);
            
            var index = this.configPanels.indexOf(configPanel);
            if(index != -1) {
                this.configPanels.splice( index, 1 );
            }

          },
        scope: this
      });
      
      if (values) {
          configPanel.onLoad(values);
      }
      
      configPanel.items.items[0].fbar.addButton(deleteButton);
        
      // ExtJS 4.2.1
      // var saveButton = configPanel.query('button[text="Save"]');
      // saveButton.hide();
      
      this.configPanels.push(configPanel);
      
      this.insert(0, configPanel);
      this.doLayout();
  },  
  
  onSubmit: function(values){
    values = {};
    values['ldap-configs'] = [];
    for (var i = 0; i < this.configPanels.length; i++) {
        values['ldap-configs'].push(this.configPanels[i].getForm().getValues());
    }
    this.el.mask(this.submitText);
    Ext.Ajax.request({
      url: restUrl + 'config/auth/ldap.json',
      method: 'POST',
      jsonData: JSON.stringify(values),
      scope: this,
      disableCaching: true,
      success: function(response){
        this.el.unmask();
      },
      failure: function(){
        this.el.unmask();
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
        // As we have a fresh load, clear out the old panels and re-create the panels based on the response.
        this.configPanels = [];
        var obj = Ext.decode(response.responseText);
        if (obj['ldap-configs'] != null) {
            var totalConfigs = obj['ldap-configs'].length

            for (var i = 0; i < totalConfigs; i++) {
                this.addNewConfig(obj['ldap-configs'][i]);
            }
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
Ext.reg("ldapMainConfigPanel", Sonia.ldap.MainConfigPanel);

// regist config panel
registerGeneralConfigPanel({
  id: 'ldapMainConfigPanel',
  xtype: 'ldapMainConfigPanel'
});
