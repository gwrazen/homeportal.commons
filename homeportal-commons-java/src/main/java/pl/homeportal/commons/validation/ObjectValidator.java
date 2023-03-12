package pl.homeportal.commons.validation;

import pl.homeportal.commons.exception.HomeportalValidationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static pl.homeportal.commons.assertions.Assert.assertGreaterThan;
import static pl.homeportal.commons.assertions.Assert.assertNotNull;

/**
 * Created by Grzegorz Wrażeń on 12-03-2023 at 11:53
 */

public class ObjectValidator
{
    private static final String GREATER_THAN = "Value: '%s' for argument: '%s' must be greater than: '%s'";
    private static final Validator VALIDATOR;

    static
    {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
    }

    public static <T> void validateWithNull(@NotNull Class<T> aClass, T object) throws HomeportalValidationException
    {
        validateNull(aClass, object);
        validateConstraints(object);
    }

    public static <T> void validateWithoutNull(@NotNull T object) throws HomeportalValidationException
    {
        validateConstraints(object);
    }

    public static void validateGreaterThan(int factor, int value, String argumentName)
    {
        try
        {
            assertGreaterThan(factor, value, argumentName);
        }
        catch (IllegalArgumentException e)
        {
            final String message = format(GREATER_THAN, value, argumentName, factor);
            throw new HomeportalValidationException(message);
        }
    }

    private static <T> HomeportalValidationException.Violation toViolation(ConstraintViolation<T> violation)
    {
        return HomeportalValidationException.Violation.of(violation.getPropertyPath().toString(),
                                                          violation.getInvalidValue(),
                                                          violation.getMessage());
    }

    private static <T> void validateNull(Class<T> aClass, T object)
    {
        try
        {
            assertNotNull(aClass, object);
        }
        catch (Exception e)
        {
            throw new HomeportalValidationException(e.getMessage(), e);
        }
    }

    private static <T> void validateConstraints(T object)
    {
        Set<ConstraintViolation<T>> cViolations = VALIDATOR.validate(object);
        Collection<HomeportalValidationException.Violation> violations = cViolations
                .stream()
                .map(v -> toViolation(v))
                .collect(toList());

        if (!cViolations.isEmpty())
        {
            throw new HomeportalValidationException(violations);
        }
    }
}
