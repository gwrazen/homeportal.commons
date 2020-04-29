package pl.homeportal.commons.mail;

import java.util.Locale;


/**
 * Created by Grzegorz Wrażeń on 10-01-2014 at 13:26
 */

public interface Notifier<T>
{
    void notify(T dto);

    boolean isEnabled();

    String getSubject(Object [] arguments, Locale locale);

    String getSenderEmail();

    String getSenderName(Locale locale);

//    String getMessage(String key, Locale locale);

    String getMessage(String key, Object [] arguments, Locale locale);
}
