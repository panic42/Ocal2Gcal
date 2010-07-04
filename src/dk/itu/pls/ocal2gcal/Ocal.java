/**
 * 
 */
package dk.itu.pls.ocal2gcal;

import java.util.logging.Logger;

import net.fortuna.ical4j.model.Calendar;

/**
 * @author panic
 *
 */
public class Ocal {
  static final Logger log =
    Logger.getLogger (Ocal.class.getName ());

  /** Perform a given operation on an Oracle calendar.
   * @param attendeeId  the attendee ID in whose calendar an entry
   *   should be handled.
   * @param userId  the user ID to use when connecting to the Oracle calendar.
   * @param serverId  the server ID identifying the Oracle calendar connection.
   * @param calendar  the calendar operation.
   */
  public void handle (
    String attendeeId, String userId, String serverId, Calendar calendar) {
    
  }
}
