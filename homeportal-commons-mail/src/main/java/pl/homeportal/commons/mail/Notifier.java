package pl.homeportal.commons.mail;

import java.util.Locale;


/**
 * Created by Grzegorz Wrażeń on 10-01-2014 at 13:26
 */

public interface Notifier<T>
{
    /**
     *
     * Always run notification in fork mode on
     */
    void notify(BaseDTO dto);

    void notify(T dto, boolean fork);

    boolean isEnabled();

    String senderEmail();

    String senderName(Locale locale);

    String message(String key, Locale locale);

    String message(String key, Object [] arguments, Locale locale);
}
