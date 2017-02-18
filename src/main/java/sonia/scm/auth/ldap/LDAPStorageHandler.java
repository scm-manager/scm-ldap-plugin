/**
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



package sonia.scm.auth.ldap;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.plugin.ext.Extension;
import sonia.scm.store.Store;
import sonia.scm.store.StoreException;
import sonia.scm.store.StoreFactory;


//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Gaurav Phadke
 */
@Singleton
public class LDAPStorageHandler
{
  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param factory
   */
  @Inject
  public LDAPStorageHandler(StoreFactory factory, String type)
  {
    store = factory.getStore(LDAPConfigList.class, type);
    legacyStore = factory.getStore(LDAPConfig.class, type);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   */
  public LDAPConfigList readConfig()
  {
    LDAPConfigList config = null;
    try {
      LDAPConfig legacyConfig = legacyStore.get();
      if (legacyConfig != null) {
        List<LDAPConfig> configList = new ArrayList<LDAPConfig>();
        configList.add(legacyConfig);
        config = new LDAPConfigList();
        config.setLDAPConfigList(configList);
        store.set(config);
      }
    } catch (StoreException se) {
      config = null;
    }

    if (config == null) {
      config = store.get();
      if (config == null)
      {
        config = new LDAPConfigList();
        store.set(config);
      }
    }
    return config;
  }

  /**
   * Method description
   *
   */
  public void storeConfig(LDAPConfigList config)
  {
    store.set(config);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Store<LDAPConfigList> store;

  /** Field description */
  private Store<LDAPConfig> legacyStore;
}
