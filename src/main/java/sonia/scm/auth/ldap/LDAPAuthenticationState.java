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

import sonia.scm.user.User;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import sonia.scm.web.security.AuthenticationResult;
import sonia.scm.web.security.AuthenticationState;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "authentication-state")
public class LDAPAuthenticationState
{

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    StringBuilder msg = new StringBuilder();

    msg.append("Authentication Result: ").append(authenticationState.toString()).append("\n");
    msg.append("Bind: ").append(String.valueOf(bind)).append("\n");
    msg.append("Search user: ").append(String.valueOf(searchUser)).append("\n");
    msg.append("Authenticate user: ").append(String.valueOf(authenticateUser)).append("\n\n");

    if (user != null)
    {
      msg.append("user: ").append(user.toString()).append("\n");
    }

    if (groups != null)
    {
      msg.append("groups: ").append(groups).append("\n");
    }

    if (exception != null)
    {
      msg.append("Exception: \n").append(exception);
    }

    return msg.toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getException()
  {
    return exception;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<String> getGroups()
  {
    return groups;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public User getUser()
  {
    return user;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isAuthenticateUser()
  {
    return authenticateUser;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isBind()
  {
    return bind;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isSearchUser()
  {
    return searchUser;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isUserValid()
  {
    return userValid;
  }

  public AuthenticationState getAuthenticationState() {
    return authenticationState;
  }
  
  public void authenticated() {
    this.authenticateUser = true;
  }
  
  public void authenticationFailed(Exception exception) {
    this.authenticateUser = false;
    failed(exception);
  }
  
  public void bind() {
    this.bind = true;
  }
  
  public void bindFailed(Exception exception) {
    this.bind = false;
    failed(exception);
  }
  
  public void searchUser() {
    this.searchUser = true;
  }
  
  public void searchUserFailed(Exception exception) {
    this.searchUser = false;
    failed(exception);
  }
  
  public void userValid() {
    this.userValid = true;
  }
  
  public void userInvalid() {
    this.userValid = false;
  }
  
  private void failed(Exception exception) {
    this.exception = exception.getMessage();
    this.authenticationState = AuthenticationState.FAILED;
  }
  
  public void sucess(User user, Collection<String> groups) {
    this.user = user;
    this.groups = groups;
    this.authenticationState = AuthenticationState.SUCCESS;
  }
  
  public AuthenticationResult createAuthenticationResult() {
    return new AuthenticationResult(user, groups, authenticationState);
  }

  //~--- fields ---------------------------------------------------------------

  private AuthenticationState authenticationState = AuthenticationState.NOT_FOUND;

  /** Field description */
  private boolean authenticateUser;

  /** Field description */
  private boolean bind;

  /** Field description */
  private String exception;

  /** Field description */
  private Collection<String> groups;

  /** Field description */
  private boolean searchUser;

  /** Only for extjs */
  private boolean success = true;

  /** Field description */
  private User user;

  /** Field description */
  private boolean userValid = false;
}
