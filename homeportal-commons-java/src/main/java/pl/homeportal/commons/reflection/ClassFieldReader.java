package pl.homeportal.commons.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Grzegorz Wrażeń on 2019-08-10
 */
public class ClassFieldReader
{
    public static Map<String, ?> readFieldValues(Object object)
    {
        // Celowo HashMap, a nie Collectors.toMap: ten drugi opiera sie na Map.merge,
        // ktory odrzuca wartosci null — czyli kazde puste pole konczylo sie NPE.
        final Map<String, Object> values = new HashMap<>();

        for (Class<?> type = object.getClass(); type != null && type != Object.class; type = type.getSuperclass())
        {
            for (Field field : type.getDeclaredFields())
            {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers()))
                {
                    continue;
                }

                values.putIfAbsent(field.getName(), readFieldValue(object, field));
            }
        }

        return values;
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
            throw new RuntimeException(e);
        }
    }
}
