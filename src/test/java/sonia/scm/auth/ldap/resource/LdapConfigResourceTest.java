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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.assertj.core.api.Assertions;
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
import sonia.scm.auth.ldap.LdapConfig;
import sonia.scm.auth.ldap.LdapConfigStore;
import sonia.scm.user.UserTestData;
import sonia.scm.web.RestDispatcher;

import javax.ws.rs.core.MediaType;
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
public class LdapConfigResourceTest {

  private static final String LDAP_CONFIG_JSON = ("{" +
          "'attributeNameFullname':'fullName'," +
          "'attributeNameId':'id'," +
          "'attributeNameMail':'mail'," +
          "'hostUrl':'http://ldap'," +
          "'searchFilter':'filter'," +
          "'searchScope':'scope'" +
          "}").replaceAll("'", "\"");

  @Rule
  public final ShiroRule shiroRule = new ShiroRule();

  @Mock
  private LdapConfigStore configStore;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;

  @Mock
  private LdapConnectionTester connectionTester;

  @InjectMocks
  private LdapConfigMapperImpl mapper;

  private RestDispatcher dispatcher;

  @Before
  public void init() {
    LdapConfigResource resource = new LdapConfigResource(configStore, mapper) {
      @Override
      LdapConnectionTester createConnectionTester(LdapConfig config) {
        return connectionTester;
      }
    };

    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);

    when(scmPathInfoStore.get()).thenReturn(() -> URI.create("/"));
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void adminShouldGetConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(configStore.get()).thenReturn(new LdapConfig());

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
    when(configStore.get()).thenReturn(new LdapConfig());

    MockHttpRequest request = MockHttpRequest.get("/v2/config/ldap");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(403, response.getStatus());
    Assertions.assertThat(response.getContentAsString())
      .contains("configuration:read:ldap");
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void adminShouldSetConfig() throws URISyntaxException {
    when(configStore.get()).thenReturn(new LdapConfig());

    MockHttpRequest request = MockHttpRequest
      .put("/v2/config/ldap")
      .contentType(MediaType.APPLICATION_JSON_TYPE)
      .content(LDAP_CONFIG_JSON.getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(204, response.getStatus());
    verify(configStore).set(any());
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void normalUserShouldNotSetConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(configStore.get()).thenReturn(new LdapConfig());

    MockHttpRequest request = MockHttpRequest
      .put("/v2/config/ldap")
      .contentType(MediaType.APPLICATION_JSON_TYPE)
      .content(LDAP_CONFIG_JSON.getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(403, response.getStatus());
    Assertions.assertThat(response.getContentAsString())
      .contains("configuration:write:ldap");
    verify(configStore, never()).set(any());
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void adminShouldTestConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(configStore.get()).thenReturn(new LdapConfig());

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
    when(configStore.get()).thenReturn(new LdapConfig());

    MockHttpRequest request = MockHttpRequest
      .put("/v2/config/ldap")
      .contentType(MediaType.APPLICATION_JSON_TYPE)
      .content(LDAP_CONFIG_JSON.getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(403, response.getStatus());
    Assertions.assertThat(response.getContentAsString())
      .contains("configuration:write:ldap");
    verify(configStore, never()).set(any());
  }
}
