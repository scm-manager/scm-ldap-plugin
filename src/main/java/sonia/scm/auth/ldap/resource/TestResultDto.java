/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.auth.ldap.resource;

import lombok.Getter;
import sonia.scm.user.User;

import java.util.Collection;
import java.util.Collections;

@Getter
public class TestResultDto {
  private final boolean configured;
  private final boolean connected;
  private final boolean userFound;
  private final boolean userAuthenticated;
  private final String exception;
  private final TestResultUserDto user;
  private final Collection<String> groups;

  TestResultDto(User user, Collection<String> groups) {
    this.configured = true;
    this.connected = true;
    this.userFound = true;
    this.userAuthenticated = true;
    this.user = new TestResultUserDto(user);
    this.exception = null;
    this.groups = groups;
  }

  TestResultDto(boolean configured, boolean connected, boolean userFound, boolean userAuthenticated, String exception) {
    this.configured = configured;
    this.connected = connected;
    this.userFound = userFound;
    this.userAuthenticated = userAuthenticated;
    this.exception = exception;
    this.user = null;
    this.groups = Collections.emptyList();
  }

  @Getter
  private static class TestResultUserDto {
    private final boolean valid;
    private final String name;
    private final String displayName;
    private final String mailAddress;

    public TestResultUserDto(User user) {
      this.valid = user.isValid();
      this.name = user.getName();
      this.displayName = user.getDisplayName();
      this.mailAddress = user.getMail();
    }
  }
}
