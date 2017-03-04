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

/**
 *
 * @author Sebastian Sdorra
 */
public class LDAPAuthenticationContext
{

  /** the logger for LDAPAuthenticationContext */
  private static final Logger logger = LoggerFactory.getLogger(LDAPAuthenticationContext.class);

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

    LDAPAuthenticator authenticator = createAuthenticator(username);
    if (authenticator == null) {
      return AuthenticationResult.NOT_FOUND;
    }
    
    AuthenticationResult result = authenticator.authenticate(username, password);
    state = authenticator.getState();
    return result;
  }
  
  private LDAPAuthenticator createAuthenticator(String username) {
    LDAPAuthenticator authenticator;
    if (isDomainForced(username)) {
      authenticator = createAuthenticatorWithForcedDomain(username);
    } else {
      authenticator = new LDAPMultiAuthenticator(config);
    }
    return authenticator;
  }
  
  private boolean isDomainForced(String username) {
    return Usernames.containsDomain(username);
  }
  
  private LDAPAuthenticator createAuthenticatorWithForcedDomain(String username) {
    String domain = Usernames.extractDomain(username).get();
    LDAPConfig cfg = findConfigForDomain(domain);
    
    if (cfg == null) {
      logger.debug("could not find configuration for forced domain {}", domain);
      return null;
    }
    
    return new LDAPSingleAuthenticator(cfg);
  }
  
  private LDAPConfig findConfigForDomain(String domain) {
    for (LDAPConfig cfg: config.getLDAPConfigList()) {
      if (cfg.isEnabled() && cfg.getUniqueId().equals(domain)) {
        return cfg;
      }
    }
    return null;
  }

  public LDAPAuthenticationState getState() {
    return state;
  }

  /** Field description */
  private final LDAPConfigList config;

  /** Field description */
  private LDAPAuthenticationState state;
}
