package pl.homeportal.commons.i18n;

import java.util.Locale;

/**
 * Created by Grzegorz Wrazen at 31-01-2014 at 23:38
 */

public enum Language
{
    POLISH("pl"),
    ENGLISH("en"),
    // ISO 639-1 dla ukrainskiego to "uk" — "ua" jest kodem KRAJU. Przy starej
    // wartosci przegladarka wysylajaca Accept-Language: uk nie byla rozpoznawana,
    // a locale() zwracalo tag nieprzypisany do zadnego jezyka.
    UKRAINIAN("uk", "ua"),
    GERMAN("de");

    private final String value;
    private final String[] aliases;

    Language(String language, String... aliases)
    {
        this.value = language;
        this.aliases = aliases;
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
            if (language.matches(value))
            {
                return language;
            }
        }

        return null;
    }

    /**
     * Aliasy istnieja dla wartosci, ktore mogly zostac utrwalone w bazie konsumenta
     * przed poprawka kodu jezyka (np. "ua" dla ukrainskiego).
     */
    private boolean matches(String candidate)
    {
        if (value.equalsIgnoreCase(candidate))
        {
            return true;
        }

        for (String alias : aliases)
        {
            if (alias.equalsIgnoreCase(candidate))
            {
                return true;
            }
        }

        return false;
    }

    public Locale locale()
    {
        return Locale.forLanguageTag(value);
    }
}
