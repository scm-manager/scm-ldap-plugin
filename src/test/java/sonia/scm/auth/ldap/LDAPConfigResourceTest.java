package sonia.scm.auth.ldap;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.apache.shiro.authz.UnauthorizedException;
import org.assertj.core.api.Assertions;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SubjectAware(configuration = "classpath:sonia/scm/shiro.ini")
@RunWith(MockitoJUnitRunner.Silent.class)
public class LDAPConfigResourceTest {

  @Rule
  public final ShiroRule shiroRule = new ShiroRule();

  @Mock
  private LDAPAuthenticationHandler authenticationHandler;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;
  @Mock
  private LDAPAuthenticationContext authenticationContext;
  @InjectMocks
  private LDAPConfigMapperImpl mapper;

  private Dispatcher dispatcher;

  @Before
  public void init() {
    LDAPConfigResource resource = new LDAPConfigResource(authenticationHandler, mapper) {
      @Override
      LDAPAuthenticationContext createContext(LDAPConfig config) {
        return authenticationContext;
      }
    };

    dispatcher = MockDispatcherFactory.createDispatcher();
    dispatcher.getRegistry().addSingletonResource(resource);

    when(scmPathInfoStore.get()).thenReturn(() -> URI.create("/"));

    dispatcher.getProviderFactory().register(new ExceptionMapper<UnauthorizedException>() {
      @Override
      public Response toResponse(UnauthorizedException e) {
        return Response.status(403).entity(e.toString()).build();
      }
    });
    when(authenticationContext.getState()).thenReturn(new LDAPAuthenticationState());
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void adminShouldGetConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(authenticationHandler.getConfig()).thenReturn(new LDAPConfig());

    MockHttpRequest request = MockHttpRequest.get("/v2/config/ldap");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(200, response.getStatus());
    Assertions.assertThat(response.getContentAsString())
      .contains("\"update\":{\"href\":\"/v2/config/ldap\"}")
      .contains("\"test\":{\"href\":\"/v2/config/ldap/test\"}")
      .contains("\"connectionPassword\":\"__DUMMY__\"");
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void normalUserShouldNotGetConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(authenticationHandler.getConfig()).thenReturn(new LDAPConfig());

    MockHttpRequest request = MockHttpRequest.get("/v2/config/ldap");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(403, response.getStatus());
    Assertions.assertThat(response.getContentAsString())
      .contains("UnauthorizedException")
      .contains("configuration:read:ldap");
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void adminShouldSetConfig() throws URISyntaxException {
    when(authenticationHandler.getConfig()).thenReturn(new LDAPConfig());

    MockHttpRequest request = MockHttpRequest
      .put("/v2/config/ldap")
      .contentType(MediaType.APPLICATION_JSON_TYPE)
      .content("{}".getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(204, response.getStatus());
    verify(authenticationHandler).setConfig(any());
    verify(authenticationHandler).storeConfig();
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void normalUserShouldNotSetConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(authenticationHandler.getConfig()).thenReturn(new LDAPConfig());

    MockHttpRequest request = MockHttpRequest
      .put("/v2/config/ldap")
      .contentType(MediaType.APPLICATION_JSON_TYPE)
      .content("{}".getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(403, response.getStatus());
    Assertions.assertThat(response.getContentAsString())
      .contains("UnauthorizedException")
      .contains("configuration:write:ldap");
    verify(authenticationHandler, never()).setConfig(any());
    verify(authenticationHandler, never()).storeConfig();
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void adminShouldTestConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(authenticationHandler.getConfig()).thenReturn(new LDAPConfig());

    MockHttpRequest request = MockHttpRequest
      .post("/v2/config/ldap/test")
      .contentType(MediaType.APPLICATION_JSON_TYPE)
      .content("{}".getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(200, response.getStatus());
    Assertions.assertThat(response.getContentAsString())
      .contains("connected")
      .contains("userFound");
    verify(authenticationHandler, never()).setConfig(any());
    verify(authenticationHandler, never()).storeConfig();
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void normalUserShouldNotTestConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(authenticationHandler.getConfig()).thenReturn(new LDAPConfig());

    MockHttpRequest request = MockHttpRequest
      .put("/v2/config/ldap")
      .contentType(MediaType.APPLICATION_JSON_TYPE)
      .content("{}".getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(403, response.getStatus());
    Assertions.assertThat(response.getContentAsString())
      .contains("UnauthorizedException")
      .contains("configuration:write:ldap");
    verify(authenticationHandler, never()).setConfig(any());
    verify(authenticationHandler, never()).storeConfig();
  }
}
