package pl.homeportal.commons.assertions;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Created by Grzegorz Wrazen on 23-11-2020
 */
public class Assert
{
    private static final String ASSERT_NOT_NULL_MESSAGE = "Object of a type: '%s' must not be null!";

    public static <T> void assertNotNull(Class<T> type, T object)
    {
        notNull(object, ASSERT_NOT_NULL_MESSAGE, type.getSimpleName());
    }
}
