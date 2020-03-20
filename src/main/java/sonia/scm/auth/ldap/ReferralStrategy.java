package sonia.scm.auth.ldap;

/**
 *
 * @author Sebastian Sdorra
 */
public enum ReferralStrategy
{

  IGNORE("ignore"), FOLLOW("follow"), THROW("throw");

  /**
   * Constructs ...
   *
   *
   * @param contextValue
   */
  private ReferralStrategy(String contextValue)
  {
    this.contextValue = contextValue;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getContextValue()
  {
    return contextValue;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String contextValue;
}
