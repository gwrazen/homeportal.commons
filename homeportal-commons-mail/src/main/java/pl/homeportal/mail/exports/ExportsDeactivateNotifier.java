package pl.homeportal.mail.exports;

import org.apache.log4j.Logger;
import pl.homeportal.service.ApplicationConfigurationService;
import pl.homeportal.i18n.Language;
import pl.homeportal.i18n.MessageSource;
import pl.homeportal.mail.EmailType;
import pl.homeportal.mail.GenericNotifier;
import pl.homeportal.mail.VelocityEmail;
import pl.homeportal.servicelocator.ServiceLocator;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.Session;

/**
 * Created by gwrazen on 23/12/2015.
 */
@Stateless
public class ExportsDeactivateNotifier extends GenericNotifier<ExportsDeactivatedDTO>
{
    private static final Logger LOG = Logger.getLogger(ExportsDeactivateNotifier.class.getSimpleName());

    private static final String SUBJECT = "email.exports.subject";
    private static final String SENDER_NAME = "email.exports.deactivated.sender.name";

    @EJB
    private ApplicationConfigurationService configuration;

    @EJB
    private MessageSource messageSource;

    @Override
    public void notify(ExportsDeactivatedDTO dto)
    {
        try
        {
            VelocityEmail email = new VelocityEmail(EmailType.EXPORTS_DEACTIVATION);
            email.setMailSession(getMailSession());
            email.setSubject(getSubject(SUBJECT, dto.getLanguage()));
            email.addTo(dto.getEmail());
            email.addCc(configuration.getEmailReceiver());
            email.setFrom(configuration.getEmailSender(), messageSource.getMessage(SENDER_NAME, dto.getLanguage()));
            email.addModel(DTO, dto);

            send(email);
        }
        catch ( Exception e )
        {
            LOG.error("Error during EXPORTS notification sending", e);
        }
    }

    @Override
    protected boolean isEnabled()
    {
        return configuration.isNotifierEnabled();
    }

    @Override
    protected String getSubject(String key, Language language)
    {
        return messageSource.getMessage(SUBJECT, language);
    }

    private Session getMailSession()
    {
        return ServiceLocator.getService(Session.class, configuration.getMailSessionJndi());
    }

}
