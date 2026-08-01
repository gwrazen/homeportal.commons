package pl.homeportal.commons.i18n;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LanguageTest
{
    @Test
    public void resolvesPolishAndEnglish()
    {
        assertEquals(Language.POLISH, Language.getByValue("pl"));
        assertEquals(Language.ENGLISH, Language.getByValue("EN"));
    }

    /**
     * Regresja: ukrainski byl zapisany jako "ua", czyli kod KRAJU. Przegladarka
     * wysylajaca Accept-Language: uk trafiala wiec na getByValue("uk") == null.
     */
    @Test
    public void resolvesUkrainianByIsoCode()
    {
        assertEquals(Language.UKRAINIAN, Language.getByValue("uk"));
    }

    @Test
    public void resolvesUkrainianByLegacyValue()
    {
        // "ua" moze byc utrwalone w bazie konsumenta sprzed poprawki
        assertEquals(Language.UKRAINIAN, Language.getByValue("ua"));
    }

    @Test
    public void resolvesUkrainianFromLocale()
    {
        assertEquals(Language.UKRAINIAN, Language.getByLocale(new Locale("uk")));
    }

    @Test
    public void localeCarriesTheIsoLanguage()
    {
        assertEquals("uk", Language.UKRAINIAN.locale().getLanguage());
    }

    @Test
    public void unknownValueResolvesToNull()
    {
        assertNull(Language.getByValue("de"));
    }
}
