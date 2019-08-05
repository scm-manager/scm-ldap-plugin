package sonia.scm.auth.ldap.resource;

import lombok.Getter;
import sonia.scm.user.User;

import java.util.Collection;
import java.util.Collections;

@Getter
public class TestResultDto {
  private final boolean connected;
  private final boolean userFound;
  private final boolean userAuthenticated;
  private final String exception;
  private final TestResultUserDto user;
  private final Collection<String> groups;

  TestResultDto(User user, Collection<String> groups) {
    this.connected = true;
    this.userFound = true;
    this.userAuthenticated = true;
    this.user = new TestResultUserDto(user);
    this.exception = null;
    this.groups = groups;
  }

  TestResultDto(boolean connected, boolean userFound, boolean userAuthenticated, String exception) {
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
