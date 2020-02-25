/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
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
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
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
