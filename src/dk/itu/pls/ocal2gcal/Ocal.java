/**
 * 
 */
package dk.itu.pls.ocal2gcal;

import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.calendar.sdk.Api;
import oracle.calendar.sdk.Handle;
import oracle.calendar.sdk.RequestResult;
import oracle.calendar.sdk.Session;
import oracle.calendar.sdk.Api.StatusException;
import oracle.calendar.soap.client.CalendarUtils;
import oracle.calendar.soap.client.CalendaringResponse;
import oracle.calendar.soap.client.Calendarlet;
import oracle.calendar.soap.client.CreateCommand;
import oracle.calendar.soap.client.Reply;
import oracle.calendar.soap.client.SearchCommand;
import oracle.calendar.soap.client.authentication.BasicAuth;
import oracle.calendar.soap.client.query.vQuery;
import oracle.calendar.soap.iCal.iCalendar;
import oracle.calendar.soap.iCal.vCalendar;
import oracle.calendar.soap.iCal.vEvent;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Uid;

/**
 * @author panic
 *
 */
public class Ocal {
  static final Logger log =
    Logger.getLogger (Ocal.class.getName ());

  Database database;
  
  /**
   * @param database
   */
  public Ocal (Database database) {
    this.database = database;
  }
  
  /** Perform a given operation on an Oracle calendar.
   * @param attendeeId  the attendee ID in whose calendar an entry
   *                  should be handled.
   * @param userId    the user ID to use when connecting to the
   *                  Oracle calendar.
   * @param serverId  the server ID identifying the Oracle calendar
   *                  connection.
   * @param calendar  the calendar operation.
   */
  public void handle (
    String attendeeId, String userId, String serverId,
    Calendar calendar) {
    
    UserCalendar userCalendar =
      database.getUserCalendar (userId, serverId);
    
    // Alternative 1: Web services auth stuff
    BasicAuth auth = new BasicAuth ();
    auth.setName (userId);
    auth.setPassword (userCalendar.passwd);
    
    // Alternative 2: Conventional Oracle Calendar auth stuff
    Session session = null;
    try {
      Api.init ("/tmp/csdk.config", "/tmp/csdk.log");
      session = new Session ();
      log.finer ("Opening connection to " + userCalendar.host);
      session.connect (Api.CSDK_FLAG_NONE, userCalendar.host);
      
      log.finer ("Authenticating with user ID " + userId + ", pass " + userCalendar.passwd);
      session.authenticate (
          Api.CSDK_FLAG_NONE, userId + "?/ND=1/", userCalendar.passwd);

      Method method = calendar.getMethod ();
    
      if (Method.REQUEST.equals (method))
        handleRequest (
          attendeeId, auth,
          userCalendar.wsServerUrl,
          session,
          calendar);
      else if (Method.CANCEL.equals (method))
        handleCancel (
          attendeeId, auth,
          userCalendar.wsServerUrl,
          userCalendar.host,
          calendar);
      else
        if (log.isLoggable (Level.FINER))
          log.finer (
            "Cannot handle VCalendar method '" + method + "'.");
    } catch (StatusException e) {
      log.throwing (Ocal.class.getName (), "handleRequest", e);
    }
  }

  void handleRequest (
      String attendeeId, BasicAuth auth,
      String serverUrl, Session session, Calendar calendar) {
    log.finer ("serverUrl = " + serverUrl);
    log.finer ("host = " + serverUrl);
    VEvent event = (VEvent) calendar.getComponent ("VEVENT");
    log.finer ("event = " + event);
    String uid = event.getUid ().getValue ();
    // 1: Get the lock from the database
    try {
      database.getLock (uid);
      try {
        // Get the authenticated user's agenda
        Handle agenda = session.getHandle (Api.CSDK_FLAG_NONE, null);
        
        // 2: Try to get the old event from the calendar server
        try {
          String oldEventStr = csdkGetEvent (session, agenda, uid);
          log.finer ("oldEventStr = " + oldEventStr);

        //Calendarlet cws = new Calendarlet ();
        //cws.setEndPointURL (serverUrl);
        //cws.setAuthenticationHeader (auth.getElement ());
        //vEvent oldEvent = cwsGetEvent (cws, uid, serverUrl, auth);
        
          // 3a: Update event details
          //vEvent newEvent = updateEvent (oldEvent, event);
        } catch (Api.StatusException e) {
          if (Api.getStatusCode (e.getStatus ()) == Api.CSDK_STAT_DATA_UID_NOTFOUND) {
            // 3b: Create new event in calendar server
            log.finer ("Creating event " + uid);
            csdkCreateEvent (session, calendar);
          } else
            log.throwing (
              Ocal.class.getName (),
              "handleRequest(1)",
              e);
        }
          // 4: Release the lock from the database
        
      } catch (StatusException e) {
        log.throwing (Ocal.class.getName (), "handleRequest(2)", e);
      } finally {
        database.releaseLock (uid);
      }
    } catch (LockFailedException e) {
      log.throwing (Ocal.class.getName (), "handleRequest(3)", e);
    }
  }

  void handleCancel (
      String attendeeId, BasicAuth auth,
      String serverUrl, String host, Calendar calendar) {
    // Remove the event from the calendar server.
  }
  
  Random random = new Random ();
  
  String csdkGetEvent (Session session, Handle agenda, String uid)
  throws StatusException {
    RequestResult result = new RequestResult ();
      session.fetchEventsByUID (
        Api.CSDK_FLAG_FETCH_RESOURCES_WITHOUT_ADDRESSES,
        agenda, 
        new String [] {uid}, 
        null, 
        Api.CSDK_THISINSTANCE, 
        new String [] {}, 
        result);
    if (log.isLoggable (Level.FINER))
      log.finer ("result = " + result);
    return result.toString ();
  }
  
  vEvent cwsGetEvent (Calendarlet cws, String uid, String serverUrl, BasicAuth auth) {
    SearchCommand search = new SearchCommand ();
    search.setCmdId (String.valueOf (random.nextInt ()));
    vQuery query = new vQuery ();
    query.setFrom (vQuery.k_queryFromEvent);
    
    // FIXME: TESTING!!!    
    uid = "20100708T142348Z-200a271-1-472bed60-Oracle";
    
//    query.setWhere ("x-oracle-data-guid = '" + uid + "'");
    java.util.Calendar friday10am =
      (java.util.Calendar) CalendarUtils.getCalendarFromUTC ("20100709T100000Z");
    java.util.Calendar friday2pm =
      (java.util.Calendar) CalendarUtils.getCalendarFromUTC ("20100709T135959Z");
    query.setWhere (
      CalendarUtils.getDateRangeQuery (friday10am, friday2pm));
    
    log.finer ("query = " + query);
    search.setQuery (query);
    CalendaringResponse response = null;
    try {
      cws.setWantIOBuffers (true);
      response = cws.Search (search.getElement ());
      if (log.isLoggable (Level.FINER)) {
        log.finer ("query = " + response.getSendBuffer ());
        log.finer ("response = " + response.getReceiveBuffer ());
      }
    } catch (Exception e) {
      log.throwing (Ocal.class.getName (), "getEvent", e);
    }
    Reply reply = (Reply) response.getCalendarReply ();
    Vector<iCalendar> iCalObjs =
      iCalendar.unmarshallVector (reply.getEntries ());
    vEvent result = null;
    for (iCalendar iCalObj : iCalObjs) {
      Vector <vCalendar> vCalObjs = iCalObj.getvCalendars ();
      for (vCalendar vCalObj : vCalObjs) {
        Vector <vEvent> vEvents = vCalObj.getComponents ();
        for (vEvent vEvent : vEvents) {
          log.finer ("vEvent = " + vEvent);
          result = vEvent;
        }
      }
    }
    return result;
  }
  
  void csdkCreateEvent (Session session, Calendar calendar) {
    RequestResult requestResult = new RequestResult ();
    try {
      final String calendarStr = calendar.toString ();
      log.finer ("Creating \n" + calendarStr);
      session.storeEvents (
        Api.CSDK_FLAG_STORE_CREATE, calendarStr, requestResult);
    } catch (StatusException e) {
      log.throwing (Ocal.class.getName (), "csdkCreateEvent", e);
    }
  }
  
  void createEvent (Calendarlet cws, VEvent event) {
    vEvent vEvent = new vEvent ();
    log.finer ("Class=" + event.getClassification ().getValue ());
    vEvent.setEventClass (event.getClassification ().getValue ());
    vEvent.setDescription (event.getDescription ().getValue ());
    vEvent.setDtStart (event.getStartDate ().getValue ());
    vEvent.setDuration (event.getDuration ().getValue ());
    vEvent.setLocation (event.getLocation ().getValue ());
    vEvent.setPriority (event.getPriority ().getValue ());
    vEvent.setSummary (event.getSummary ().getValue ());
    vEvent.setUid (event.getUid ().getValue ());
    
    log.finer ("vEvent = " + vEvent.toString ());
    
    vCalendar vCalendar = new vCalendar ();
    vCalendar.addvComponent (vEvent);
    
    iCalendar iCalendar = new iCalendar ();
    iCalendar.addvCalendar (vCalendar);
    
    CreateCommand create = new CreateCommand ();
    create.setCmdId (String.valueOf (random.nextInt ()));
    create.setiCalendar (iCalendar);
    
  }
}
