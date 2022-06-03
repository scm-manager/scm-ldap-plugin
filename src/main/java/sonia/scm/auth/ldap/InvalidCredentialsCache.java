package sonia.scm.auth.ldap;

import com.google.inject.Inject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

class InvalidCredentialsCache {

  private final Cache<UsernamePasswordKey, Object> cache;
  private final MessageDigest passwordCacheDigest;

  @Inject
  InvalidCredentialsCache(CacheManager cacheManager) throws NoSuchAlgorithmException {
    this.cache = cacheManager.getCache("sonia.scm.ldap.invalidCredentials");
    this.passwordCacheDigest = MessageDigest.getInstance("SHA-256");
  }

  void verifyNotInvalid(UsernamePasswordToken token) {
    if (cache.contains(new UsernamePasswordKey(token))) {
      throw new AuthenticationException("this is known to be wrong");
    }
  }

  void cacheAsInvalid(UsernamePasswordToken upt) {
    cache.put(new UsernamePasswordKey(upt), new Object());
  }

  private class UsernamePasswordKey {
    private final String username;
    private final byte[] password;

    private UsernamePasswordKey(UsernamePasswordToken token) {
      this.username = token.getUsername();
      this.password = passwordCacheDigest.digest(toBytes(token.getPassword()));
    }

    private byte[] toBytes(char[] chars) {
      CharBuffer charBuffer = CharBuffer.wrap(chars);
      ByteBuffer byteBuffer = UTF_8.encode(charBuffer);
      byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
        byteBuffer.position(), byteBuffer.limit());
      Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
      return bytes;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;

      if (!(o instanceof UsernamePasswordKey)) return false;

      UsernamePasswordKey that = (UsernamePasswordKey) o;

      return new EqualsBuilder().append(username, that.username).append(password, that.password).isEquals();
    }

    @Override
    public int hashCode() {
      return new HashCodeBuilder(17, 37).append(username).append(password).toHashCode();
    }
  }
}
