package pl.homeportal.commons.assertions;

import java.util.Objects;

import static java.lang.String.format;

/**
 * Created by Grzegorz Wrazen on 23-11-2020
 */
public class Assert
{
    private static final String ASSERT_NOT_NULL_MESSAGE = "Object of a type: '%s' must not be null!";
    private static final String ASSERT_GREATER_THAN_MESSAGE = "Argument '%s' with value '%s' must be greater than '%s'!";

    public static <T> void assertNotNull(Class<T> type, T object)
    {
        if (Objects.isNull(object))
        {
            final String message = format(ASSERT_NOT_NULL_MESSAGE, type.getSimpleName());
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertGreaterThan(int factor, int value, String argumentName)
    {
        if (!(value > factor))
        {
            final String message = format(ASSERT_GREATER_THAN_MESSAGE, argumentName, value, factor);
            throw new IllegalArgumentException(message);
        }
    }
}
