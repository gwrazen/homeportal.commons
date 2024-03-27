package pl.homeportal.commons.mvc.controller;

import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Created by Grzegorz Wrażeń on 29-08-2021 at 13:42
 */

public interface FormAwareController
{
    String BINDING_RESULTS_FORM = "org.springframework.validation.BindingResult.form";
    String FORM = "form";

    @InitBinder
    void initBinder(WebDataBinder binder);

    ModelAndView formBackingObject(ModelMap model);

    default void saveFlashAttributes(Object form, BindingResult result, RedirectAttributes redirectAttributes)
    {
        redirectAttributes.addFlashAttribute(BINDING_RESULTS_FORM, result);
        redirectAttributes.addFlashAttribute(FORM, form);
    }
}
