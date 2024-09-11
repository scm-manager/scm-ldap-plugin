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

import sonia.scm.security.CipherUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Sebastian Sdorra
 */
public class XmlCipherAdapter extends XmlAdapter<String, String>
{

  /** Field description */
  private static final String PREFIX = "{enc}";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   *
   * @throws Exception
   */
  @Override
  public String marshal(String v) throws Exception
  {
    if (!isEncrypted(v))
    {
      v = PREFIX.concat(CipherUtil.getInstance().encode(v));
    }

    return v;
  }

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   *
   * @throws Exception
   */
  @Override
  public String unmarshal(String v) throws Exception
  {
    if (isEncrypted(v))
    {
      v = CipherUtil.getInstance().decode(v.substring(PREFIX.length()));
    }

    return v;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   */
  private boolean isEncrypted(String v)
  {
    return Util.isNotEmpty(v) && v.startsWith(PREFIX);
  }
}
