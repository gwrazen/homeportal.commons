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
        LocaleResolver localeResolver = (LocaleResolver) request.getAttribute(LOCALE_RESOLVER_ATTRIBUTE);
        if (localeResolver != null)
        {
            Locale locale = localeResolver.resolveLocale(request);
            return Language.getByValue(locale.getLanguage());
        }

        return null;
    }

    public static Locale resolveLocale(HttpServletRequest request)
    {
        LocaleResolver localeResolver = (LocaleResolver) request.getAttribute(LOCALE_RESOLVER_ATTRIBUTE);
        if (localeResolver != null)
        {
            return localeResolver.resolveLocale(request);
        }

        return null;
    }

    public static Locale locale()
    {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return LanguageResolver.resolveLocale(request);
    }
}
