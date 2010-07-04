/**
 * 
 */
package dk.itu.pls.ocal2gcal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/** Object for extracting calendar entries from MIME emails.
 * An object created from an email delivered-to
 * attendeeid#userid#serverid@... will have its fields set
 * accordingly.
 * @author panic
 */
public class CalendarEmail {
  static final Logger log =
    Logger.getLogger (CalendarEmail.class.getName ());

  private InputStream calendarInputStream = null;
  private String attendeeId = null;
  private String userId = null;
  private String serverId = null;
  /** Get the contents of the calendar part of the MIME email message.
   * @return the calendarInputStream
   */
  public InputStream getCalendarInputStream () {
    return calendarInputStream;
  }
  /**
   * @return the attendeeId
   */
  public String getAttendeeId () {
    return attendeeId;
  }
  /**
   * @return the userId
   */
  public String getUserId () {
    return userId;
  }
  /**
   * @return the serverId
   */
  public String getServerId () {
    return serverId;
  }
  private CalendarEmail () { }
  /** Parse an InputStream as a MIME email message, picking out
   * a calendar entry part.
   * @param is  the MIME email message.
   * @return an object containing the attendee, user and server IDs,
   *   and the contents of the calendar entry part.
   * @throws NoCalendarPartException  if no MIME message part
   *   containing a calendar entry could be found in the 
   *   message.
   * @throws MalformedDeliveredToException  if the delivered-to
   * header line does not have the form attendeeId#userId#serverId@smtphost.
   */
  public static CalendarEmail parseEmail (InputStream is)
  throws NoCalendarPartException, MalformedDeliveredToException  {
    CalendarEmail email = new CalendarEmail ();
    Session session =
      Session.getDefaultInstance (new Properties ());
    try {
      MimeMessage msg = new MimeMessage (session, is);
      parseDeliveredTo (msg.getHeader ("delivered-to", null), email);
      if (msg.isMimeType ("multipart/mixed")) {
        MimeMultipart multipart = (MimeMultipart) msg.getContent ();
        int parts = multipart.getCount ();
        for (int i = 0; i < parts; i++) {
          BodyPart part = multipart.getBodyPart (i);
          if (part.isMimeType ("text/calendar") ||
              part.isMimeType ("application/ics")) {
            email.calendarInputStream =
              part.getInputStream ();
            return email;
          }
        }
      }
    } catch (MessagingException e) {
      log.throwing (CalendarEmail.class.getName (), "parseEmail", e);
    } catch (IOException e) {
      log.throwing (CalendarEmail.class.getName (), "parseEmail", e);
    }
    throw
      new NoCalendarPartException (
        "Could not find a text/calendar MIME message part");
  }
  
  final static Pattern deliveredToPattern =
    Pattern.compile ("^([^#@]+)#([^#@]+)#([^#@]+)@[^@]+$");
  
  static void parseDeliveredTo (String deliveredTo, CalendarEmail email) 
  throws MalformedDeliveredToException {
    final Matcher deliveredToMatcher =
      deliveredToPattern.matcher (deliveredTo);
    if (deliveredToMatcher.matches ()) {
      email.attendeeId = deliveredToMatcher.group (1);
      email.userId = deliveredToMatcher.group (2);
      email.serverId = deliveredToMatcher.group (3);
    } else
    throw new MalformedDeliveredToException (
      "Expected '<attid>#<userid>#<serverid>@<smtphost>' but got '" +
      deliveredTo + "'");
  }
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString () {
    return attendeeId + "#" + userId + "#" + serverId + ":\n" +
    calendarInputStream;
  }
}
