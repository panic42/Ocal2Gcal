/**
 * 
 */
package dk.itu.pls.ocal2gcal;

/** Exception indicating that an attempt to obtain a
 * lock in an SQL database failed.
 * @author panic
 *
 */
public class LockFailedException extends Exception {

  /**
   * 
   */
  public LockFailedException () {
  }

  /**
   * @param message
   */
  public LockFailedException (String message) {
    super (message);
  }

  /**
   * @param cause
   */
  public LockFailedException (Throwable cause) {
    super (cause);
  }

  /**
   * @param message
   * @param cause
   */
  public LockFailedException (String message, Throwable cause) {
    super (message, cause);
  }

}
