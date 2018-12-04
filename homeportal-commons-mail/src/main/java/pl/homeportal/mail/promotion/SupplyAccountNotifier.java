package pl.homeportal.mail.promotion;

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
 * Created by gwrazen on 10/02/2015.
 */
@Stateless(name = "SupplyAccountNotifier")
public class SupplyAccountNotifier extends GenericNotifier<SupplyAccountDTO>
{
    private static final Logger LOG = Logger.getLogger(SupplyAccountNotifier.class.getSimpleName());

    private static final String SUBJECT = "email.promotion.supply.subject";
    private static final String SENDER_NAME = "email.promotion.supply.sender.name";

    @EJB
    private ApplicationConfigurationService configuration;

    @EJB
    private MessageSource messageSource;

    @Override
    public void notify(SupplyAccountDTO dto)
    {
        try
        {
            VelocityEmail email = new VelocityEmail(EmailType.SUPPLY_ACCOUNT);
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
            LOG.error("Error during SUPPLY ACCOUNT notification sending", e);
        }
    }

    @Override
    protected boolean isEnabled()
    {
        return configuration.isNotifierEnabled() && configuration.isNotifierAccountSupplyEnabled();
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
