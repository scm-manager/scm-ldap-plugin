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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.auth.ldap.LdapConfig;
import sonia.scm.auth.ldap.LdapConfigStore;
import sonia.scm.auth.ldap.LdapConnectionFactory;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.user.User;
import sonia.scm.web.VndMediaType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Optional;

import static sonia.scm.auth.ldap.resource.LdapModule.PERMISSION_NAME;

@OpenAPIDefinition(tags = {
  @Tag(name = "LDAP Plugin", description = "LDAP Plugin related endpoints")
})
@Singleton
@Path("v2/config/ldap")
public class LdapConfigResource {

  private final LdapConfigStore configStore;
  private final LdapConfigMapper mapper;
  private final LdapConnectionFactory ldapConnectionFactory;

  @Inject
  public LdapConfigResource(LdapConfigStore configStore, LdapConfigMapper mapper, LdapConnectionFactory ldapConnectionFactory) {
    this.configStore = configStore;
    this.mapper = mapper;
    this.ldapConnectionFactory = ldapConnectionFactory;
  }

  @POST
  @Path("test")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Test ldap configuration", description = "Tests ldap configuration.", tags = "LDAP Plugin")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = LdapTestConfigDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public TestResultDto testConfig(LdapTestConfigDto testConfig) {
    ConfigurationPermissions.write(PERMISSION_NAME).check();
    LdapConfig config = mapper.map(testConfig.getConfig(), configStore.get());

    LdapConnectionTester tester = createConnectionTester(config);

    AuthenticationResult result = tester.test(testConfig.getUsername(), testConfig.getPassword());
    Optional<User> user = result.getUser();
    Optional<AuthenticationFailure> failureOptional = result.getFailure();

    if (user.isPresent()) {
      return new TestResultDto(user.get(), result.getGroups());
    } else {
      AuthenticationFailure failure = failureOptional.orElseThrow(() -> new IllegalStateException("no user and no failure"));
      return new TestResultDto(
        failure.isConfigured(),
        failure.isConnected(),
        failure.isUserFound(),
        failure.isUserAuthenticated(),
        failure.getException()
      );
    }
  }

  @VisibleForTesting
  LdapConnectionTester createConnectionTester(LdapConfig config) {
    return new LdapConnectionTester(ldapConnectionFactory, config);
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get ldap configuration", description = "Returns the ldap configuration.", tags = "LDAP Plugin")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = LdapConfigDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public LdapConfigDto getConfig() {
    ConfigurationPermissions.read(PERMISSION_NAME).check();
    return mapper.map(configStore.get());
  }

  @PUT
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get ldap configuration", description = "Returns the ldap configuration.", tags = "LDAP Plugin")
  @ApiResponse(responseCode = "204", description = "success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response setConfig(@Context UriInfo uriInfo, @NotNull @Valid LdapConfigDto config) {
    ConfigurationPermissions.write(PERMISSION_NAME).check();
    LdapConfig newConfig = mapper.map(config, configStore.get());
    configStore.set(newConfig);

    return Response.noContent().build();
  }
}
