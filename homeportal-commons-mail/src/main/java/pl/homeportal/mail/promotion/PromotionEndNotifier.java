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
 * Created by gwrazen on 07/02/2015.
 */
@Stateless(name = "PromotionEndNotifier")
public class PromotionEndNotifier extends GenericNotifier<PromotionEndDTO>
{
    private static final Logger LOG = Logger.getLogger(PromotionEndNotifier.class.getSimpleName());

    private static final String SUBJECT = "email.promotion.ended.subject";
    private static final String SENDER_TITLE = "email.promotion.ended.sender.name";

    @EJB
    private ApplicationConfigurationService configuration;

    @EJB
    private MessageSource messageSource;

    @Override
    public void notify(PromotionEndDTO dto)
    {
        try
        {
            VelocityEmail email = new VelocityEmail(EmailType.PROMOTION_END);
            email.setMailSession(getMailSession());
            email.setSubject(getSubject(SUBJECT, dto.getLanguage()));
            email.addTo(dto.getEmail());
            email.addCc(configuration.getEmailReceiver());
            email.setFrom(configuration.getEmailSender(), messageSource.getMessage(SENDER_TITLE, dto.getLanguage()));
            email.addModel(DTO, dto);

            send(email);
        }
        catch ( Exception e )
        {
            LOG.error("Error during PROMOTION END notification sending", e);
        }
    }

    @Override
    protected boolean isEnabled()
    {
        return configuration.isNotifierEnabled() && configuration.isNotifierPromotionEndEnabled();
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
