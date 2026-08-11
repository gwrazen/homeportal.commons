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

    /**
     * Wysylka synchroniczna, ktora ZWRACA wynik — jedyny wariant, po ktorym wolajacy wie,
     * czy mail faktycznie wyszedl.
     *
     * Pozostale warianty tego nie daja: {@code notify(dto)} oddaje wysylke puli watkow, a wyjatek
     * ze srodka jest lapany i tylko logowany, wiec do wolajacego nie wraca nic — takze przy
     * {@code notify(dto, false)}. Przez to np. panel moderatora stemplowal date wyslania maila
     * rowniez wtedy, gdy SMTP odmowil.
     *
     * @return {@code true} wylacznie gdy mail zostal wyslany; {@code false} gdy notifier jest
     *         wylaczony ({@link #isEnabled()}) albo wysylka rzucila. Wyjatek nie wychodzi na
     *         zewnatrz — wolajacy dostaje wynik, nie blad do obsluzenia.
     */
    boolean notifyChecked(BaseDTO dto);

    boolean isEnabled();

    String senderEmail();

    String senderName(Locale locale);

    String message(String key, Locale locale);

    String message(String key, Object [] arguments, Locale locale);
}
