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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.AssertUtil;
import sonia.scm.web.security.AuthenticationResult;
import sonia.scm.web.security.AuthenticationState;

/**
 *
 * @author Sebastian Sdorra
 */
public class LDAPAuthenticationContext
{

  /** Field description */
  public static final String ATTRIBUTE_GROUP_NAME = "cn";

  /** Field description */
  public static final String NESTEDGROUP_MATCHINGRULE =
    ":1.2.840.113556.1.4.1941:=";

  /** Field description */
  public static final String SEARCHTYPE_GROUP = "group";

  /** Field description */
  public static final String SEARCHTYPE_USER = "user";

  /** the logger for LDAPContext */
  private static final Logger logger =
    LoggerFactory.getLogger(LDAPAuthenticationContext.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param config
   */
  public LDAPAuthenticationContext(LDAPConfigList config)
  {
    this.config = config;
    this.state = new LDAPAuthenticationState();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param username
   * @param password
   *
   * @return
   */
  public AuthenticationResult authenticate(String username, String password)
  {
    AssertUtil.assertIsNotEmpty(username);
    AssertUtil.assertIsNotEmpty(password);

    // If we have a forced domain in the username field then we should use that.
    AuthenticationResult result;
    if (isDomainForced(username)) {
      result = authenticateWithForcedDomain(username, password);
    } else {
      result = authenticateConfigList(username, password);
    }
    return result;
  }
  
  private boolean isDomainForced(String username) {
    return username.contains("\\");
  }
  
  private AuthenticationResult authenticateWithForcedDomain(String username, String password){
    String[] domainAndUsername = username.split("\\\\");
    String domain = domainAndUsername[0];
    String user = domainAndUsername[1];
    LDAPConfig cfg = findConfigForDomain(domain);
    
    if (cfg == null) {
      logger.debug("we could not find configuration for forced domain %s", domain);
      return AuthenticationResult.NOT_FOUND;
    }
    
    return authenticate(cfg, user, password);
  }
  
  private LDAPConfig findConfigForDomain(String domain) {
    for(LDAPConfig cfg: config.getLDAPConfigList()) {
      if (cfg.isEnabled() && cfg.getUniqueId().equals(domain)) {
        return cfg;
      }
    }
    return null;
  }
  
  private AuthenticationResult authenticateConfigList(String username, String password){
    AuthenticationResult result = AuthenticationResult.NOT_FOUND;
    
    for (LDAPConfig subConfig: config.getLDAPConfigList()) {
      if (subConfig.isEnabled()) {
        result = authenticate(subConfig, username, password);

        // If we have not found the user in this config then we
        // should try the remaining configurations. Else we
        // should just return the result.
        if (result.getState() != AuthenticationState.NOT_FOUND) {
          break;
        }
      }
    }
    
    return result;
  }

  /**
   * Method description
   *
   *
   * @param username
   * @param password
   * @param config
   *
   * @return
   */
  public AuthenticationResult authenticate(LDAPConfig config, String username, String password)
  {
    LDAPSingleAuthenticator authenticator = new LDAPSingleAuthenticator(config, username, password);
    AuthenticationResult result = authenticator.authenticate();
    state = authenticator.getState();
    return result;
  }

  public LDAPAuthenticationState getState() {
    return state;
  }

  /** Field description */
  private LDAPConfigList config;

  /** Field description */
  private LDAPAuthenticationState state;
}
