package pl.homeportal.commons.mail;

import java.util.Locale;


/**
 * Created by Grzegorz Wrażeń on 10-01-2014 at 13:26
 */

public interface Notifier<T>
{
    void notify(T dto);

    boolean isEnabled();

    String senderEmail();

    String senderName(Locale locale);

    String message(String key, Object [] arguments, Locale locale);
}
