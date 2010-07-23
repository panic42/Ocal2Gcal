package dk.itu.pls.ocal2gcal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/** Class for containing all the configuration settings of the program.
 * These settings are read at startup from a properties file.
 * @author panic
 *
 */
public class Settings {
  static final Logger log =
    Logger.getLogger (Settings.class.getName ());
  /** Host name of the SQL server. */
  public static final String sqlServer =
    getProperties ().getProperty ("ocal2gcal.sql.server", "localhost");
  /** User ID to access the SQL server. */
  public static final String sqlUserId =
    getProperties ().getProperty ("ocal2gcal.sql.userid", "ocal2gcal");
  /** User password to access the SQL server. */
  public static final String sqlPasswd =
    getProperties ().getProperty ("ocal2gcal.sql.password", "s8Quarck");
  /** Name of the database containing OCal2GCal data. */
  public static final String sqlDatabase =
    getProperties ().getProperty ("ocal2gcal.sql.database", "OCal2GCal");
  
  private static Properties properties = null;
  /**
   * @return the Properties object used for fetching property values.
   */
  private static Properties getProperties () {
    if (properties != null) return properties;
    try {
      InputStream propertiesStream =
        Settings.class.getResourceAsStream ("/properties");
      if (propertiesStream != null) {
        properties = new Properties ();
        properties.load (propertiesStream);
      }
    } catch (IOException e) {
      log.throwing (Settings.class.getName (), "getProperties", e);
    }
    return properties;
  }

}
