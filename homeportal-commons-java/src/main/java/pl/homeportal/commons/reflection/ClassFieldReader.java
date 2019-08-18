package pl.homeportal.commons.reflection;

import pl.homeportal.commons.exception.HomeportalSystemException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Grzegorz Wrażeń on 2019-08-10
 */
public class ClassFieldReader
{
    public static Map<String, ?> readFieldValues(Object object)
    {
        return Arrays.stream(object.getClass().getDeclaredFields())
                .collect(Collectors.toMap(field -> field.getName(), field -> readFieldValue(object, field)));
    }

    private static Object readFieldValue(Object object, Field f)
    {
        try
        {
            f.setAccessible(true);
            return f.get(object);
        }
        catch (IllegalAccessException e)
        {
            throw new HomeportalSystemException(e);
        }
    }
}
