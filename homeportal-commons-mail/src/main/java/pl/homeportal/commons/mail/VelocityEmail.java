package pl.homeportal.commons.mail;

import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Session;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import static pl.homeportal.commons.text.Constants.EMPTY_STRING;

/**
 * Created by Grzegorz Wrazen on 16-11-2013 at 19:10
 */

public class VelocityEmail extends HtmlEmail
{
    private static final Logger LOG = LoggerFactory.getLogger(VelocityEmail.class.getSimpleName());

    private static final String ENCODING = "UTF-8";
    private static final String INCORRECT_ADDRESS = "Incorrect address: %s";
    private static final VelocityEngine VELOCITY_ENGINE = new VelocityEngine();
    private static final String DTO = "dto";

    @Getter
    private final EmailTemplate template;
    private final VelocityContext context;

    static
    {
        VELOCITY_ENGINE.addProperty("resource.loader", "class");
        VELOCITY_ENGINE.addProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        VELOCITY_ENGINE.addProperty("runtime.log.logsystem.log4j.logger", "root");
        VELOCITY_ENGINE.addProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
        VELOCITY_ENGINE.init();
    }

    private VelocityEmail(EmailTemplate template)
    {
        super();
        this.template = template;
        this.setCharset(ENCODING);
        this.context = new VelocityContext();
    }

    public static VelocityEmail of(EmailTemplate template)
    {
        return new VelocityEmail(template);
    }

    public VelocityEmail session(Session session)
    {
        this.setMailSession(session);
        return this;
    }

    public VelocityEmail subject(String subject)
    {
        this.setSubject(subject);
        return this;
    }

    public VelocityEmail from(String fromEmail)
    {
        try
        {
            this.setFrom(fromEmail);
            return this;
        }
        catch (EmailException e)
        {
            LOG.warn(String.format(INCORRECT_ADDRESS, fromEmail), e);
            throw new RuntimeException(e);
        }
    }

    public VelocityEmail from(String fromEmail, String displayName)
    {
        try
        {
            this.setFrom(fromEmail, displayName);
            return this;
        }
        catch (EmailException e)
        {
            LOG.warn(String.format(INCORRECT_ADDRESS, fromEmail), e);
            throw new RuntimeException(e);
        }
    }

    public VelocityEmail tos(Set<String> tos)
    {
        if (CollectionUtils.isEmpty(tos))
        {
            return this;
        }

        tos.forEach(address -> {
            try
            {
                this.addTo(address);
            }
            catch (EmailException e)
            {
                LOG.warn(String.format(INCORRECT_ADDRESS, address), e);
            }
        });
        return this;
    }

    public VelocityEmail ccs(Set<String> tos)
    {
        if (CollectionUtils.isEmpty(tos))
        {
            return this;
        }

        tos.forEach(address -> {
            try
            {
                this.addCc(address);
            }
            catch (EmailException e)
            {
                LOG.warn(String.format(INCORRECT_ADDRESS, address), e);
            }
        });
        return this;
    }

    public VelocityEmail bccs(Set<String> tos)
    {
        if (CollectionUtils.isEmpty(tos))
        {
            return this;
        }

        tos.forEach(address -> {
            try
            {
                this.addBcc(address);
            }
            catch (EmailException e)
            {
                LOG.warn(String.format(INCORRECT_ADDRESS, address), e);
            }
        });
        return this;
    }

    public VelocityEmail attachments(Set<BaseDTO.Attachment> attachments)
    {
        if (CollectionUtils.isEmpty(attachments))
        {
            return this;
        }

        attachments.forEach(attachment ->
        {
            try
            {
                URL url = resolveUrl(attachment);
                this.attach(url, attachment.getName(), EMPTY_STRING);
            }
            catch (Exception e)
            {
                LOG.warn(e.getMessage());
            }
        });

        return this;
    }

    public VelocityEmail model(BaseDTO model)
    {
        this.context.put(DTO, model);
        return this;
    }

    @Override
    public String send()
    {
        StringWriter writer = null;

        try
        {
            writer = new StringWriter();
            getTemplate().merge(context, writer);
            setHtmlMsg(writer.toString());
            return super.send();
        }
        catch (Exception e)
        {
            LOG.error("Problem during sending email.", e);
            return null;
        }
        finally
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }

    public String getTemplateName()
    {
        return template.getTemplateName();
    }

    private Template getTemplate()
    {
        return VELOCITY_ENGINE.getTemplate(getTemplateName(), ENCODING);
    }

    private URL resolveUrl(BaseDTO.Attachment attachment) throws MalformedURLException
    {
        if (attachment.isResources())
        {
            return getClass().getClassLoader().getResource(attachment.getPath());
        }
        return new URL(attachment.getPath());
    }
}