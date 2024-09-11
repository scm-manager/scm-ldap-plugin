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

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Sdorra
 */
public class XmlCipherAdapterTest {

  @Test
  public void testMarshall() throws Exception {
    assertThat("test", not(equalTo(adapter.marshal("test"))));
    assertThat("{enc}ajhsbd", equalTo(adapter.marshal("{enc}ajhsbd")));
  }

  @Test
  public void testUnmarshall() throws Exception {
    assertThat("test", equalTo(adapter.unmarshal("test")));

    String e = adapter.marshal("test");

    assertThat("test", not(equalTo(e)));
    assertThat("test", equalTo(adapter.unmarshal(e)));
  }

  //~--- fields ---------------------------------------------------------------

  private XmlCipherAdapter adapter = new XmlCipherAdapter();
}
