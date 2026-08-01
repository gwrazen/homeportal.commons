package pl.homeportal.commons.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;
import pl.homeportal.commons.exception.HomeportalValidationException;

import javax.validation.constraints.NotNull;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class ObjectValidatorTest
{

    public static final Integer ONE = 1;

    @Test(expected = HomeportalValidationException.class)
    public void validateWithNullObjectNull()
    {
        final TestObject objectNull = null;
        ObjectValidator.validateWithNull(TestObject.class, objectNull);
    }

    @Test
    public void validateWithNullObjectNotNull()
    {
        TestObject object = TestObject.of(ONE);
        ObjectValidator.validateWithNull(TestObject.class, object);
        assertNotNull(object.getId());
    }

    @Test
    public void validateWithNullConstraintViolationNull()
    {
        TestObject object = TestObject.of(null);
        try
        {
            ObjectValidator.validateWithNull(TestObject.class, object);
            throw new AssertionError("Expected HomeportalValidationException for a null @NotNull field");
        }
        catch (HomeportalValidationException e)
        {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void validateWithoutNull()
    {
        TestObject object = TestObject.of(ONE);
        ObjectValidator.validateWithoutNull(object);
        assertNotNull(object.getId());
    }

    @Test
    public void validateWithoutNullConstraintViolationNull()
    {
        TestObject object = TestObject.of(null);
        try
        {
            ObjectValidator.validateWithoutNull(object);
            throw new AssertionError("Expected HomeportalValidationException for a null @NotNull field");
        }
        catch (HomeportalValidationException e)
        {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void validateGreaterThan()
    {
        final int factor = 10;
        final int value = 11;
        final String message = "powers";
        ObjectValidator.validateGreaterThan(factor, value, message);
    }

    @Test
    public void validateGreaterThanConstraintViolation()
    {
        final int factor = 10;
        final int value = 9;
        final String argumentName = "powers";
        try
        {
            ObjectValidator.validateGreaterThan(factor, value, argumentName);
            throw new AssertionError("Expected HomeportalValidationException for a value below the factor");
        }
        catch (HomeportalValidationException e)
        {
            assertThat(e.getMessage(), containsString(argumentName));
        }
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    static class TestObject
    {
        @NotNull
        private Integer id;
    }
}
