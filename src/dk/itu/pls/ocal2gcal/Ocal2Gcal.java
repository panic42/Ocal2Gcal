/**
 * 
 */
package dk.itu.pls.ocal2gcal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

/**
 * @author panic
 *
 */
public class Ocal2Gcal {
  static final Logger log =
    Logger.getLogger (Ocal2Gcal.class.getName ());
  /** Take a MIME email message on STDIN, or optionally from the
   * file named in the first argument, and apply it to the 
   * Oracle calendar.
   * @param args
   */
  public static void main (String [] args) {
    try {
      final InputStream propertiesStream =
        Ocal2Gcal.class.getResourceAsStream ("/logging.properties");
      if (propertiesStream != null)
        LogManager.getLogManager ().
          readConfiguration (propertiesStream);
    } catch (Exception e) {
      log.throwing (Ocal2Gcal.class.getName (), "main", e);
    }
    if (args.length > 0) {
      String inFilename = args [0];
      try {
        InputStream is = new FileInputStream (inFilename);
        System.setIn (is);
      } catch (FileNotFoundException e) {
        log.warning ("Cannot open file '" + inFilename + "', using stdin.");
      }
    }
    CalendarBuilder builder = new CalendarBuilder ();
    try {
      // Fetch the calendar entry from STDIN
      CalendarEmail email = CalendarEmail.parseEmail (System.in);
      String attendeeId = email.getAttendeeId ();
      String userId = email.getUserId ();
      String serverId = email.getServerId ();
      InputStream calendarInputStream = email.getCalendarInputStream ();
      log.fine (email.toString ());
      Calendar calendar = builder.build (calendarInputStream);
      log.fine (calendar.toString ());
      // Apply the calendar entry to the appropriate Oracle calendar
      //Ocal.handle (attendeeId, userId, serverId, calendar);
    } catch (IOException e) {
      log.throwing (Ocal2Gcal.class.getName (), "main", e);
    } catch (ParserException e) {
      log.throwing (Ocal2Gcal.class.getName (), "main", e);
    } catch (NoCalendarPartException e) {
      log.throwing (Ocal2Gcal.class.getName (), "main", e);
    } catch (MalformedDeliveredToException e) {
      log.throwing (Ocal2Gcal.class.getName (), "main", e);
    }
  }

}
