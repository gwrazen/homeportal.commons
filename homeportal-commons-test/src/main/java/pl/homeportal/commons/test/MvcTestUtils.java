package pl.homeportal.commons.test;

import org.springframework.test.web.servlet.ResultActions;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.List;

/**
 * Created by Grzegorz Wrażeń on 11-06-2023 at 10:32
 */

public class MvcTestUtils
{
    public static BindingResult bindingResult(String key, ResultActions result)
    {
        return BindingResult.class.cast(result.andReturn().getFlashMap().get(key));
    }

    public static List<ObjectError> errors(String key, ResultActions result)
    {
        return BindingResult.class.cast(result.andReturn().getFlashMap().get(key)).getAllErrors();
    }

    public static List<FieldError> fieldErrors(String key, ResultActions result)
    {
        return BindingResult.class.cast(result.andReturn().getFlashMap().get(key)).getFieldErrors();
    }

    public static List<ObjectError> globalErrors(String key, ResultActions result)
    {
        return BindingResult.class.cast(result.andReturn().getFlashMap().get(key)).getGlobalErrors();
    }

    public static int countErrors(String key, ResultActions result)
    {
        return BindingResult.class.cast(result.andReturn().getFlashMap().get(key)).getErrorCount();
    }

    public static int countGlobalErrors(String key, ResultActions result)
    {
        return BindingResult.class.cast(result.andReturn().getFlashMap().get(key)).getGlobalErrorCount();
    }

    public static int countFieldErrors(String key, ResultActions result)
    {
        return BindingResult.class.cast(result.andReturn().getFlashMap().get(key)).getFieldErrorCount();
    }

    public static <T> T fromModel(String key, ResultActions result, Class<T> clazz)
    {
        return clazz.cast(result.andReturn().getModelAndView().getModel().get(key));
    }
}
