package pl.homeportal.commons.mail;

import lombok.Getter;
import pl.homeportal.commons.i18n.Language;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Grzegorz Wrażeń on 24-02-2020
 */

@Getter
public abstract class BaseDTO
{
    public static final int INITIAL_CAPACITY = 1;

    private final Language language;
    private Set<String> tos;
    private Set<String> ccs;
    private Set<String> bccs;

    protected BaseDTO(Language language)
    {
        this.language = language;
    }

    public BaseDTO addTo(String email)
    {
        if (tos == null)
        {
            tos = new HashSet<>(INITIAL_CAPACITY);
        }

        tos.add(email);
        return this;
    }

    public BaseDTO addCc(String email)
    {
        if (ccs == null)
        {
            ccs = new HashSet<>(INITIAL_CAPACITY);
        }

        ccs.add(email);
        return this;
    }

    public BaseDTO addBcc(String email)
    {
        if (bccs == null)
        {
            bccs = new HashSet<>(INITIAL_CAPACITY);
        }

        bccs.add(email);
        return this;
    }

    public Locale locale()
    {
        return language.locale();
    }
}
