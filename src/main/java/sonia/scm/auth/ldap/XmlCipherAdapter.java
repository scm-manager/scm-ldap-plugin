
package sonia.scm.auth.ldap;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.security.CipherUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.adapters.XmlAdapter;

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
