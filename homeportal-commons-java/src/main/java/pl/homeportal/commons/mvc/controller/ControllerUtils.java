package pl.homeportal.commons.mvc.controller;

import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

import java.util.HashSet;
import java.util.Set;

import static java.lang.Boolean.TRUE;

/**
 * Created by Grzegorz Wrażeń on 04-03-2023 at 09:52
 */

public class ControllerUtils
{
    private static final Set<String> RESOURCE_SUFFIXES = new HashSet<>(5);

    static
    {
        RESOURCE_SUFFIXES.add("/logo");
        RESOURCE_SUFFIXES.add("logo/");
        RESOURCE_SUFFIXES.add(".jpeg");
        RESOURCE_SUFFIXES.add(".jpg");
        RESOURCE_SUFFIXES.add(".png");
    }

    public static String getParameter(String key)
    {
        return currentRequest().getParameter(key);
    }

    public static HttpServletRequest currentRequest()
    {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static String currentUri()
    {
        return currentRequest().getRequestURI();
    }

    public static ModelAndView modelAndView(String view)
    {
        return new ModelAndView(view);
    }

    public static ModelAndView modelAndView(String view, ModelMap model)
    {
        return new ModelAndView(view, model);
    }

    public static ModelAndView modelAndView(String view, Model model)
    {
        return new ModelAndView(view, model.asMap());
    }

    public static ModelAndView redirectView(String view)
    {
        return new ModelAndView(new RedirectView(view, TRUE));
    }

    public static boolean isResourceUriSuffix()
    {
        final String uri = currentRequest().getRequestURI();
        for (String suffix : RESOURCE_SUFFIXES)
        {
            if (uri.endsWith(suffix))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isNotResourceUriSuffix()
    {
        return !isResourceUriSuffix();
    }
}
