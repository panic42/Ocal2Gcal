package dk.itu.pls.ocal2gcal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;



/** Object for interacting with the SQL database.
 * @author panic
 *
 */
public class Database {
  static final Logger log =
    Logger.getLogger (Database.class.getName ());

  final String server;
  final String database;
  final String username;
  final String passwd;
  
  Connection con;

  /** 
   * @param server    the hostname of the database server
   * @param database  the name of the database in the server
   * @param username  the user ID with which to log into the database
   * @param passwd    the password with which to log into the database
   */
  public Database (String server, String database, String username, String passwd) {
    this.server = server;
    this.database = database;
    this.username = username;
    this.passwd = passwd;
    try {
      con = openConnection ();
    } catch (SQLException e) {
      log.throwing (Database.class.getName (), "Database", e);
    }
  }

  private Connection openConnection ()
  throws SQLException {
    String url = "jdbc:mysql://" + server + "/" + database;
    Connection c;
    Properties props = new Properties ();
    props.setProperty ("user", username);
    props.setProperty ("password", passwd);
    props.setProperty ("autoReconnect", "true");
    props.setProperty ("useUnicode", "true");
    props.setProperty ("characterEncoding", "UTF-8");
    log.info ("Opening MySQL connection to " + url + " using properties " + props + "...");
    log.fine ("Free memory: " + Runtime.getRuntime ().freeMemory ());
    c = DriverManager.getConnection (url, props);
    log.fine ("Free memory: " + Runtime.getRuntime ().freeMemory ());
    return c;
  }

  /** Execute an SQL query, reopening the database connection if
   * necessary. 
   * @param query  the SQL query to execute
   * @return  a Statement object containing the result, or null 
   *          if the query caused an SQL exception 
   */
  protected Statement execute (String query) {
    Statement stmt = null;
    try {
      stmt = con.createStatement ();
      stmt.execute (query);
      return stmt;
    } catch (SQLException e) {
      try {
        log.throwing (Database.class.getName (), "execute(1)", e);
        if (!con.isValid (0)) con = openConnection ();
        stmt = con.createStatement ();
        stmt.execute (query);
        return stmt;
      } catch (SQLException e1) {
        log.throwing (Database.class.getName (), "execute(2)", e1);
      }
    }
    return null;
  }
  
  /** Get the Oracle Calendar password for a given user at a
   * given Oracle Calendar server.
   * @param userId    The user's login at the calendar server
   * @param serverId  The calendar server id
   * @return  The user's password and the calendar server's url
   */
  public UserCalendar getUserCalendar (String userId, String serverId) {
    UserCalendar result = null;
    String query =
    	"SELECT password, serverurl, host FROM UserCalendars" +
    	" WHERE serverid = '" + serverId + "'" +
      "   AND login = '" + userId + "'";
    Statement stmt = execute (query);
    if (stmt != null) {
      ResultSet rows;
      try {
        rows = stmt.getResultSet ();
        if (rows.next ())
          result =
            new UserCalendar (
              rows.getString (1),
              rows.getString (2),
              rows.getString (3));
      } catch (SQLException e) {
        log.throwing (Database.class.getName (), "getPassword", e);
      }
    }
    return result;
  }
  
  public void getLock (String id) throws LockFailedException {
    String query = "SELECT GET_LOCK ('" + id + "', 10000)";
    final Statement stmt = execute (query);
    try {
      if (stmt != null) {
        ResultSet rows;
          rows = stmt.getResultSet ();
        rows.next ();
        if (rows.getBoolean (1)) return;
      }
    } catch (SQLException e) {
      log.throwing (Database.class.getName (), "getLock", e);
    }
    throw new LockFailedException ("LOCK_GET failed for ID " + id);
  }

  public void releaseLock (String id) {
    String query = "SELECT RELEASE_LOCK ('" + id + "')";
    execute (query);
  }
}
