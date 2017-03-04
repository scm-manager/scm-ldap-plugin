/**
 * Copyright (c) 2014, Sebastian Sdorra
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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Utils methods for handling usernames.
 * 
 * @author Sebastian Sdorra
 */
public final class Usernames {

  private static final String DOMAIN_SEPERATOR = "\\\\";
  
  private Usernames() {
  }
  
  /**
   * Returns {@code true} if the username contains a domain.
   * 
   * @param username raw username
   * 
   * @return {@code true} if username contains domain part
   */
  public static boolean containsDomain(String username) {
    if ( ! Strings.isNullOrEmpty(username) ) {
      String[] parts = parseUsername(username);
      return containsDomainPart(parts);
    }
    return false;
  }
  
  /**
   * Extracts the domain part from the username.
   * 
   * @param username raw username
   * 
   * @return optional domain part
   */
  public static Optional<String> extractDomain(String username) {
    String[] parts = parseUsername(username);
    if ( containsDomainPart(parts) ) {
      return Optional.of(parts[0]);
    }
    return Optional.absent();
  }
  
  /**
   * Returns username without domain part.
   * 
   * @param username raw username
   * 
   * @return username without domain
   */
  public static String withoutDomain(String username) {
    String[] parts = parseUsername(username);
    return parts[parts.length - 1];
  }
  
  private static boolean containsDomainPart(String[] parts) {
    return parts.length > 1 && !Strings.isNullOrEmpty(parts[0]);
  }
  
  private static String[] parseUsername(String username) {
    checkUsernameParameter(username);
    return split(username);
  }
  
  private static void checkUsernameParameter(String username) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(username), "username is required");
  }
  
  private static String[] split(String username) {
    return username.split(DOMAIN_SEPERATOR);
  }
}
