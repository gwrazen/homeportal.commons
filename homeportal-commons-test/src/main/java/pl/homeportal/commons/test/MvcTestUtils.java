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
    public static BindingResult bindingResult(String key, ResultActions resultActions)
    {
        return BindingResult.class.cast(resultActions.andReturn().getFlashMap().get(key));
    }

    public static List<ObjectError> bindingErrors(String key, ResultActions resultActions)
    {
        return BindingResult.class.cast(resultActions.andReturn().getFlashMap().get(key)).getAllErrors();
    }

    public static List<FieldError> bindingFieldErrors(String key, ResultActions resultActions)
    {
        return BindingResult.class.cast(resultActions.andReturn().getFlashMap().get(key)).getFieldErrors();
    }

    public static int errorCount(String key, ResultActions resultActions)
    {
        return BindingResult.class.cast(resultActions.andReturn().getFlashMap().get(key)).getErrorCount();
    }

    public static int fieldErrorCount(String key, ResultActions resultActions)
    {
        return BindingResult.class.cast(resultActions.andReturn().getFlashMap().get(key)).getFieldErrorCount();
    }

    public static <T> T fromModel(String key, ResultActions resultActions, Class<T> clazz)
    {
        return clazz.cast(resultActions.andReturn().getModelAndView().getModel().get(key));
    }
}
