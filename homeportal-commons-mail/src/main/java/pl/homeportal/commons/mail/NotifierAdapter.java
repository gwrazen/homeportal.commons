package pl.homeportal.commons.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import javax.mail.Session;
import java.util.Locale;

import static java.lang.String.format;

/**
 * Created by Grzegorz Wrażeń on 29-02-2020
 */

public abstract class NotifierAdapter<T extends BaseDTO> implements Notifier<BaseDTO>
{
    private static final Logger LOG = LoggerFactory.getLogger(NotifierAdapter.class.getSimpleName());

    protected Session session;
    protected MessageSource messageSource;

    protected abstract EmailTemplate template();

    @Override
    public void notify(BaseDTO dto)
    {
        try
        {
            final VelocityEmail email = VelocityEmail.of(template())
                    .session(session)
                    .subject(getSubject(dto.locale()))
                    .from(getSenderEmail(), getSenderName(dto.locale()))
                    .tos(dto.getTos())
                    .ccs(dto.getCcs())
                    .bccs(dto.getBccs())
                    .model(dto);

            send(email);
        }
        catch (Exception e)
        {
            LOG.error(format("Error during notification sending for template: %s", template()), e);
        }
    }

    @Override
    public String getMessage(String key, Locale locale)
    {
        return messageSource.getMessage(key, null, locale);
    }

    private void send(VelocityEmail email)
    {
        if (isEnabled())
        {
            String response = email.send();
            LOG.info(format("Email sent. Template: %s, response: %s", email.getTemplateName(), response));
            return;
        }

        LOG.info(format("Notifier disabled for email type: %s", email.getTemplateName()));
    }
}
