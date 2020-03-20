package sonia.scm.auth.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

class AutoCloseableNamingEnumeration<T> implements NamingEnumeration<T>, AutoCloseable {

  private final NamingEnumeration<T> original;

  AutoCloseableNamingEnumeration(NamingEnumeration<T> original) {
    this.original = original;
  }

  @Override
  public T next() throws NamingException {
    return original.next();
  }

  @Override
  public boolean hasMore() throws NamingException {
    return original.hasMore();
  }

  @Override
  public void close() throws NamingException {
    original.close();
  }

  @Override
  public boolean hasMoreElements() {
    return original.hasMoreElements();
  }

  @Override
  public T nextElement() {
    return original.nextElement();
  }
}
