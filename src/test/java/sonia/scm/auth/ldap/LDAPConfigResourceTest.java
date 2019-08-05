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
import sonia.scm.user.UserTestData;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

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
  private LDAPConfigStore configStore;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;

  @Mock
  private LdapConnectionTester connectionTester;

  @InjectMocks
  private LDAPConfigMapperImpl mapper;

  private Dispatcher dispatcher;

  @Before
  public void init() {
    LDAPConfigResource resource = new LDAPConfigResource(configStore, mapper) {
      @Override
      LdapConnectionTester createConnectionTester(LDAPConfig config) {
        return connectionTester;
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
    // when(connectionTester.getState()).thenReturn(new LDAPAuthenticationState());
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void adminShouldGetConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(configStore.get()).thenReturn(new LDAPConfig());

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
    when(configStore.get()).thenReturn(new LDAPConfig());

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
    when(configStore.get()).thenReturn(new LDAPConfig());

    MockHttpRequest request = MockHttpRequest
      .put("/v2/config/ldap")
      .contentType(MediaType.APPLICATION_JSON_TYPE)
      .content("{}".getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(204, response.getStatus());
    verify(configStore).set(any());
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void normalUserShouldNotSetConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(configStore.get()).thenReturn(new LDAPConfig());

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
    verify(configStore, never()).set(any());
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void adminShouldTestConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(configStore.get()).thenReturn(new LDAPConfig());

    AuthenticationResult result = new AuthenticationResult(UserTestData.createSlarti(), Collections.emptySet());
    when(connectionTester.test(any(), any())).thenReturn(result);

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
    verify(configStore, never()).set(any());
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void normalUserShouldNotTestConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(configStore.get()).thenReturn(new LDAPConfig());

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
    verify(configStore, never()).set(any());
  }
}
