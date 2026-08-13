package pl.homeportal.commons.i18n;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE;

/**
 * Created by Grzegorz Wrazen on 19-02-2014 at 12:31
 */

public class LanguageResolver
{
    public static Language resolveLanguage(HttpServletRequest request)
    {
        if (request == null)
        {
            return Language.POLISH;
        }

        LocaleResolver localeResolver = (LocaleResolver) request.getAttribute(LOCALE_RESOLVER_ATTRIBUTE);
        if (localeResolver != null)
        {
            Locale locale = localeResolver.resolveLocale(request);
            return orDefault(Language.getByValue(locale.getLanguage()));
        }

        // Brak LocaleResolver zdarza sie poza DispatcherServletem (filtr, dispatch
        // typu ERROR, zasoby statyczne) — wtedy jezyk bierzemy wprost z zadania.
        return orDefault(Language.getByValue(request.getLocale().getLanguage()));
    }

    /**
     * Jezyk nierozpoznany schodzi na polski, a nie na null. Wynik tej metody leci
     * prosto do zapytan po kolumnie Language (cechy oferty, meta SEO), gdzie null
     * konczyl sie pustym wynikiem albo NPE — a zadanie z Accept-Language spoza
     * naszej listy jest normalnym ruchem z internetu, nie bledem konfiguracji.
     */
    private static Language orDefault(Language language)
    {
        return language == null ? Language.POLISH : language;
    }

    public static Locale resolveLocale(HttpServletRequest request)
    {
        if (request == null)
        {
            return Locale.getDefault();
        }

        LocaleResolver localeResolver = (LocaleResolver) request.getAttribute(LOCALE_RESOLVER_ATTRIBUTE);
        if (localeResolver != null)
        {
            return localeResolver.resolveLocale(request);
        }

        return request.getLocale();
    }

    public static Locale locale()
    {
        if (RequestContextHolder.getRequestAttributes() == null)
        {
            return Locale.getDefault();
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return LanguageResolver.resolveLocale(request);
    }
}
