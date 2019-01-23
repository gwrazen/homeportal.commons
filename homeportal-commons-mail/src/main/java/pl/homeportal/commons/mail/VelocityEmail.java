package pl.homeportal.commons.mail;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: gwrazen
 * Date: 16/11/13
 * Time: 19:10
 * To change this template use File | Settings | File Templates.
 */
public class VelocityEmail extends HtmlEmail
{
    private static final Logger LOG = LoggerFactory.getLogger(VelocityEmail.class.getSimpleName());

    private static final String ENCODING = "UTF-8";
    private static final VelocityEngine VELOCITY_ENGINE = new VelocityEngine();

    private VelocityContext context = new VelocityContext();

    private final EmailType emailType;

    static
    {
        VELOCITY_ENGINE.addProperty("resource.loader", "class");
        VELOCITY_ENGINE.addProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        VELOCITY_ENGINE.addProperty("runtime.log.logsystem.log4j.logger", "root");
        VELOCITY_ENGINE.addProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
        VELOCITY_ENGINE.init();
    }

    public VelocityEmail(EmailType emailType) throws EmailException
    {
        super();
        this.emailType = emailType;
        setCharset(ENCODING);
        setSmtpPort(465);
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
                catch (IOException e){}
            }
        }
    }

    public void addModel(String key, Object value)
    {
        context.put(key, value);
    }

    public EmailType getEmailType()
    {
        return emailType;
    }

    private Template getTemplate()
    {
        return VELOCITY_ENGINE.getTemplate(emailType.getTemplateName(), ENCODING);
    }

}