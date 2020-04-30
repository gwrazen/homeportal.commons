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

    private static final String SENDER_NAME = "email.sender.name";

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
            LOG.error(format("Error during notification sending for template: %s", template()), e);
        }
    }

    @Override
    public void notify(BaseDTO dto, boolean fork)
    {
        this.fork = fork;
        notify(dto);
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
            LOG.info(format("Notifier disabled for email type: %s", template()));
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
        final VelocityEmail email = createVelocityEmail(dto);
        final String response = email.send();
        LOG.info(format("Email sent. Template: %s, response: %s", template(), response));
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
                .model(dto);
    }

    private String subject(BaseDTO dto)
    {
        return message(dto.subjectKey(), dto.subjectArguments(), dto.locale());
    }
}
