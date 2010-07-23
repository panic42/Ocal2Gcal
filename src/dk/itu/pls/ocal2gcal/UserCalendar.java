package dk.itu.pls.ocal2gcal;

public class UserCalendar {
  /** Password for this user on the Oracle Calendar server. */
  public final String passwd;
  /** URL for the web services Oracle Calendar server API. */
  public final String wsServerUrl;
  /** Host name for the Oracle Calendar server API.
   * This can be an IP address or host name, and can
   * optionally include a local port.
   */
  public final String host;
  /** Construct a UserCalendar object.
   * @param passwd
   * @param serverUrl
   */
  public UserCalendar (String passwd, String serverUrl, String host) {
    this.passwd = passwd;
    this.wsServerUrl = serverUrl;
    this.host = host;
  }
}
