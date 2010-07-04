/**
 * 
 */
package dk.itu.pls.ocal2gcal;

/**
 * @author panic
 *
 */
public class MalformedDeliveredToException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1438412523316535373L;

  /**
   * @param message
   * @param cause
   */
  public MalformedDeliveredToException (String message, Throwable cause) {
    super (message, cause);
  }

  /**
   * @param message
   */
  public MalformedDeliveredToException (String message) {
    super (message);
  }

  /**
   * @param cause
   */
  public MalformedDeliveredToException (Throwable cause) {
    super (cause);
  }

}
