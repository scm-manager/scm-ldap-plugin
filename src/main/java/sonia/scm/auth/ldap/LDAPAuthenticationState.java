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

//~--- JDK imports ------------------------------------------------------------

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author Sebastian Sdorra
 */
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

    msg.append("Bind: ").append(String.valueOf(bind)).append("\n").append(
        "Search user: ").append(String.valueOf(searchUser)).append("\n").append(
        "Authenticate user: ").append(String.valueOf(authenticateUser)).append(
        "\n");

    if (exception != null)
    {
      StringWriter writer = new StringWriter();

      exception.printStackTrace(new PrintWriter(writer));
      msg.append("Exception: \n").append(writer.toString());
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
  public Exception getException()
  {
    return exception;
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

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param authenticateUser
   */
  public void setAuthenticateUser(boolean authenticateUser)
  {
    this.authenticateUser = authenticateUser;
  }

  /**
   * Method description
   *
   *
   * @param bind
   */
  public void setBind(boolean bind)
  {
    this.bind = bind;
  }

  /**
   * Method description
   *
   *
   * @param exception
   */
  public void setException(Exception exception)
  {
    this.exception = exception;
  }

  /**
   * Method description
   *
   *
   * @param searchUser
   */
  public void setSearchUser(boolean searchUser)
  {
    this.searchUser = searchUser;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean authenticateUser;

  /** Field description */
  private boolean bind;

  /** Field description */
  private Exception exception;

  /** Field description */
  private boolean searchUser;
}
