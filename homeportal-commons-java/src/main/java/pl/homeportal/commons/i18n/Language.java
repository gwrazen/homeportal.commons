package pl.homeportal.commons.i18n;

import java.util.Locale;

/**
 * Created by Grzegorz Wrazen at 31-01-2014 at 23:38
 */

public enum Language
{
    POLISH("pl"),
    ENGLISH("en"),
    UKRAINIAN("ua");

    private String value;

    Language(String language)
    {
        this.value = language;
    }

    public String getValue()
    {
        return value;
    }

    public static Language getByLocale(Locale locale)
    {
        return getByValue(locale.getLanguage());
    }

    public static Language getByValue(String value)
    {
        for (Language language : values())
        {
            if (language.getValue().equalsIgnoreCase(value))
            {
                return language;
            }
        }

        return null;
    }

    public Locale locale()
    {
        return Locale.forLanguageTag(value);
    }
}
