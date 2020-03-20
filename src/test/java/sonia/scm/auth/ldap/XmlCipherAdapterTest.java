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
