package pl.homeportal.commons.mail;

import lombok.Setter;
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

    // default sender name key
    private static final String SENDER_NAME = "email.sender.name";

    protected Session session;
    protected MessageSource messageSource;

    @Setter
    protected boolean fork = true;

    protected abstract EmailTemplate template();

    @Override
    public void notify(BaseDTO dto)
    {
        try
        {
            final VelocityEmail email = VelocityEmail.of(template())
                    .session(session)
                    .subject(getSubject(dto.subjectArguments(), dto.locale()))
                    .from(getSenderEmail(), getSenderName(dto.locale()))
                    .tos(dto.getTos())
                    .ccs(dto.getCcs())
                    .bccs(dto.getBccs())
                    .attachments(dto.getAttachments())
                    .model(dto);

            send(email);
        }
        catch (Exception e)
        {
            LOG.error(format("Error during notification sending for template: %s", template()), e);
        }
    }

//    @Override
//    public String getMessage(String key, Locale locale)
//    {
//        return messageSource.getMessage(key, null, locale);
//    }

    @Override
    public String getMessage(String key, Object [] arguments, Locale locale)
    {
        return messageSource.getMessage(key, arguments, locale);
    }

    @Override
    public String getSenderName(Locale locale)
    {
        return getMessage(SENDER_NAME, null, locale);
    }

    private void send(VelocityEmail email)
    {
        if (!isEnabled())
        {
            LOG.info(format("Notifier disabled for email type: %s", email.getTemplateName()));
            return;
        }

        if (!fork)
        {
            String response = email.send();
            LOG.info(format("Email sent. Template: %s, response: %s", email.getTemplateName(), response));
            return;
        }

        new Thread(() ->
        {
            String response = email.send();
            LOG.info(format("Email sent. Template: %s, response: %s", email.getTemplateName(), response));
            return;

        }).start();
    }
}
