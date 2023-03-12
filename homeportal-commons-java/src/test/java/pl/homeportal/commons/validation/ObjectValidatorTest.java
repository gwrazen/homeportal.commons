package pl.homeportal.commons.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;
import pl.homeportal.commons.exception.HomeportalValidationException;

import javax.validation.constraints.NotNull;

import static org.junit.Assert.fail;

public class ObjectValidatorTest
{

    public static final Integer ONE = 1;

    @Test(expected = HomeportalValidationException.class)
    public void validateWithNullObjectNull()
    {
        final TestObject objectNull = null;
        ObjectValidator.validateWithNull(TestObject.class, objectNull);
        fail();
    }

    @Test
    public void validateWithNullObjectNotNull()
    {
        TestObject object = TestObject.of(ONE);
        ObjectValidator.validateWithNull(TestObject.class, object);
    }

    @Test(expected = HomeportalValidationException.class)
    public void validateWithNullConstraintViolationNull()
    {
        TestObject object = TestObject.of(null);
        ObjectValidator.validateWithNull(TestObject.class, object);
        fail();
    }

    @Test
    public void validateWithoutNull()
    {
        TestObject object = TestObject.of(ONE);
        ObjectValidator.validateWithNull(TestObject.class, object);
    }

    @Test(expected = HomeportalValidationException.class)
    public void validateWithoutNullConstraintViolationNull()
    {
        TestObject object = TestObject.of(null);
        ObjectValidator.validateWithNull(TestObject.class, object);
        fail();
    }

    @Test
    public void validateGreaterThan()
    {
        final int factor = 10;
        final int value = 11;
        final String message = "powers";
        ObjectValidator.validateGreaterThan(factor, value, message);
    }

    @Test(expected = HomeportalValidationException.class)
    public void validateGreaterThanConstraintViolation()
    {
        final int factor = 10;
        final int value = 9;
        final String message = "powers";
        ObjectValidator.validateGreaterThan(factor, value, message);
        fail();
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    static class TestObject
    {
        @NotNull
        private Integer id;
    }
}