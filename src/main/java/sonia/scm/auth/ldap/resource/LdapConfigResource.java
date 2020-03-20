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
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.user.User;
import sonia.scm.web.VndMediaType;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
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

  @Inject
  public LdapConfigResource(LdapConfigStore configStore, LdapConfigMapper mapper) {
    this.configStore = configStore;
    this.mapper = mapper;
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
    return new LdapConnectionTester(config);
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
