package pl.homeportal.commons.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Session;
import java.util.Properties;

/**
 * Created by Grzegorz Wrażeń 18-01-2014 at 18:28
 */

public class SessionMockProvider
{
    private static final Logger LOG = LoggerFactory.getLogger(SessionMockProvider.class);

    public Session getSession()
    {
        Session s = null;
        try
        {
            s = Session.getDefaultInstance(new Properties());
        }
        catch (Exception e)
        {
            LOG.error("Could not create a mock mail session", e);
        }
        return s;
    }
}
