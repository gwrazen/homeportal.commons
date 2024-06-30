package pl.homeportal.commons.mail;

import org.slf4j.Logger;
import org.springframework.context.MessageSource;

import javax.mail.Session;
import java.util.Locale;

import static java.lang.String.format;
import static pl.homeportal.commons.logging.LoggingSupport.error;
import static pl.homeportal.commons.logging.LoggingSupport.information;
import static pl.homeportal.commons.logging.LoggingSupport.logger;
import static pl.homeportal.commons.mail.NotifierAdapter.Messages.NOTIFICATION_DISABLED_MSG;
import static pl.homeportal.commons.mail.NotifierAdapter.Messages.NOTIFICATION_SENT_MSG;
import static pl.homeportal.commons.mail.NotifierAdapter.Messages.SENDER_NAME;

/**
 * Created by Grzegorz Wrażeń on 29-02-2020
 */

public abstract class NotifierAdapter<T extends BaseDTO> implements Notifier<BaseDTO>
{
    private static final Logger LOG = logger(NotifierAdapter.class);

    protected Session session;
    protected MessageSource messageSource;

    private boolean fork = true;

    protected abstract EmailTemplate template();

    @Override
    public void notify(BaseDTO dto)
    {
        try
        {
            send(dto);
        }
        catch (Exception e)
        {
            error(LOG, e, format(Error.NOTIFICATION_SENDING_ERR, template()));
        }
    }

    @Override
    public void notify(BaseDTO dto, boolean fork)
    {
        this.fork = fork;
        notify(dto);
    }

    @Override
    public String message(String key, Locale locale)
    {
        return messageSource.getMessage(key, null, locale);
    }

    @Override
    public String message(String key, Object[] arguments, Locale locale)
    {
        return messageSource.getMessage(key, arguments, locale);
    }

    @Override
    public String senderName(Locale locale)
    {
        return message(SENDER_NAME, null, locale);
    }

    private void send(BaseDTO dto)
    {
        if (!isEnabled())
        {
            information(LOG, format(NOTIFICATION_DISABLED_MSG, template()));
            return;
        }

        if (!fork)
        {
            createAndSend(dto);
            return;
        }

        new Thread(() ->
                   {
                       createAndSend(dto);
                   }).start();
    }

    private void createAndSend(BaseDTO dto)
    {
        try
        {
            final VelocityEmail email = createVelocityEmail(dto);
            final String response = email.send();
            information(LOG, format(NOTIFICATION_SENT_MSG, template(), response));
        }
        catch (Exception e)
        {
            error(LOG, e, Error.CREATE_AND_SEND_ERR);
        }
    }

    private VelocityEmail createVelocityEmail(BaseDTO dto)
    {
        return VelocityEmail.of(template())
                .session(session)
                .subject(subject(dto))
                .from(senderEmail(), senderName(dto.locale()))
                .tos(dto.getTos())
                .ccs(dto.getCcs())
                .bccs(dto.getBccs())
                .attachments(dto.getAttachments())
                .embedded(dto.getEmbedded())
                .model(dto);

    }

    private String subject(BaseDTO dto)
    {
        return message(dto.subjectKey(), dto.subjectArguments(), dto.locale());
    }

    static class Messages
    {
        public static final String SENDER_NAME = "email.sender.name";
        public static final String NOTIFICATION_SENT_MSG = "Email sent. Template: %s, response: %s";
        public static final String NOTIFICATION_DISABLED_MSG = "Notifications disabled for email type: %s";

    }

    static class Error
    {
        public static final String NOTIFICATION_SENDING_ERR = "Error during notification sending for template: %s";
        public static final String CREATE_AND_SEND_ERR = "Error during createAndSend";

    }
}
