package sonia.scm.auth.ldap;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
@ExtendWith(MockitoExtension.class)
class LdapRealmTest extends LdapServerTestBaseJunit5 {

  @Mock
  private LdapConfigStore configStore;

  @Mock
  private SyncingRealmHelper syncingRealmHelper;

  @InjectMocks
  private LdapRealm realm;

  private LdapConfig config;

  @BeforeEach
  void setUpRealm() {
    config = createConfig();
    when(configStore.get()).thenReturn(config);
  }

  @Test
  void shouldReturnNullIfLdapIsDisabled() {
    ldif(1);
    config.setEnabled(false);

    AuthenticationInfo authenticationInfo = realm.doGetAuthenticationInfo(createToken("trillian", "trilli123"));

    assertThat(authenticationInfo).isNull();
  }

  @Test
  void shouldThrowUnknownAccountException() {
    ldif(1);

    assertThrows(UnknownAccountException.class, () -> realm.doGetAuthenticationInfo(createToken("hansolo", "trilli123")));
  }

  @Test
  void testSimpleAuthentication() {
    ldif(1);

    AuthenticationInfo authenticationInfoMock = mock(AuthenticationInfo.class);
    when(syncingRealmHelper.createAuthenticationInfo(eq(LdapRealm.TYPE), any())).thenReturn(authenticationInfoMock);

    AuthenticationInfo authenticationInfo = realm.doGetAuthenticationInfo(createToken("trillian", "trilli123"));
    verify(syncingRealmHelper).store(any(User.class));
    assertThat(authenticationInfo).isSameAs(authenticationInfoMock);
  }

  @Test
  void testWrongPassword() {
    ldif(1);

    assertThrows(UserAuthenticationFailedException.class, () -> realm.doGetAuthenticationInfo(createToken("trillian", "trilli1234")));
  }

  private AuthenticationToken createToken(String username, String password) {
    return new UsernamePasswordToken(username, password);
  }
}

