package pl.homeportal.mail.portals;

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
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.mail.Session;

/**
 * Created by grzechu on 09.06.2017.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PortalsActivationNotifier extends GenericNotifier<PortalsActivatedDTO>
{
    private static final Logger LOG = Logger.getLogger(PortalsDeactivationNotifier.class.getSimpleName());

    private static final String SUBJECT = "email.portals.activated.subject";
    private static final String SENDER_NAME = "email.portals.sender.name";

    @EJB
    private ApplicationConfigurationService configuration;

    @EJB
    private MessageSource messageSource;

    @Override
    public void notify(PortalsActivatedDTO dto)
    {
        try
        {
            VelocityEmail email = new VelocityEmail(EmailType.PORTALS_ACTIVATION);
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
            LOG.error("Error during PORTALS OFFERS notification sending", e);
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
        return messageSource.getMessage(key, language);
    }

    private Session getMailSession()
    {
        return ServiceLocator.getService(Session.class, configuration.getMailSessionJndi());
    }

}
