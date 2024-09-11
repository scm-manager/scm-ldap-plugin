/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
