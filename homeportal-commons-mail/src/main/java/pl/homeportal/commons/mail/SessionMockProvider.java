package pl.homeportal.commons.mail;

import javax.mail.Session;
import java.util.Properties;

/**
 * Created by Grzegorz Wrażeń 18-01-2014 at 18:28
 */

public class SessionMockProvider
{
    public Session getSession()
    {
        Session s = null;
        try
        {
            s = Session.getDefaultInstance(new Properties());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return s;
    }
}
