package pl.homeportal.commons.i18n;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by gwrazen on 08/02/2015.
 */
//@Component
public class MessageSource
{
    private static final Logger LOG = LoggerFactory.getLogger(MessageSource.class.getSimpleName());

    private static final String MESSAGES = "messages";

    public String getMessage(String key, Language language)
    {
        try
        {
            ResourceBundle bundle = ResourceBundle.getBundle(MESSAGES, Locale.forLanguageTag(language.getValue()));
            return bundle.getString(key);
        }
        catch (Exception e)
        {
            LOG.warn("Could not find ResourceBundle " + e.getMessage(), e);
            return null;
        }
    }
}
