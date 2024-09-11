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
