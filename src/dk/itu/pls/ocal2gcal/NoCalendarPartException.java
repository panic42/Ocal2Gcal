/**
 * 
 */
package dk.itu.pls.ocal2gcal;

/** Exception indicating that no MIME message part containing a
 * calendar entry could be found.
 * @author panic
 *
 */
public class NoCalendarPartException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -3374673022612562507L;

  /**
   * @param message
   */
  public NoCalendarPartException (String message) {
    super (message);
  }

  /**
   * @param cause
   */
  public NoCalendarPartException (Throwable cause) {
    super (cause);
  }

  /**
   * @param message
   * @param cause
   */
  public NoCalendarPartException (String message, Throwable cause) {
    super (message, cause);
  }

}
