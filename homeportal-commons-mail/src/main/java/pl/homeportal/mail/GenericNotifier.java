package pl.homeportal.mail;

import org.apache.log4j.Logger;
import pl.homeportal.i18n.Language;

import javax.mail.Session;


/**
 * Created with IntelliJ IDEA.
 * User: gwrazen
 * Date: 10/01/14
 * Time: 13:26
 * To change this template use File | Settings | File Templates.
 */
public abstract class GenericNotifier<T>
{
    private static final Logger LOG = Logger.getLogger(GenericNotifier.class.getSimpleName());

    protected static final String DTO = "dto";

    protected Session mailSession;

    protected abstract void notify(T dto);

    protected abstract boolean isEnabled();

    protected abstract String getSubject(String key, Language language);

    protected void send(VelocityEmail email)
    {
        if ( isEnabled() )
        {
            String response = email.send();
            LOG.info("Email sent. Template: " + email.getEmailType().getTemplateName() + ", response: " + response);
            return;
        }

        LOG.info(String.format("Notifier disabled for email type: %s", email.getEmailType().getTemplateName()));
    }

    protected void setMailSession(Session mailSession)
    {
        this.mailSession = mailSession;
    }
}
