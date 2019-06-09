package pl.homeportal.commons.mail;

import javax.mail.Session;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: gwrazen
 * Date: 18/01/14
 * Time: 18:28
 * To change this template use File | Settings | File Templates.
 */
public class SessionMockProvider
{
    public Session getSession()
    {
        Session s = null;
        try {
            s = Session.getDefaultInstance(new Properties());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }
}
