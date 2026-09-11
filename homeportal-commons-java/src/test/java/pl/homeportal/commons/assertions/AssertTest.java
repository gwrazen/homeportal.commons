package pl.homeportal.commons.assertions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssertTest
{
    @Test
    public void assertNotNull()
    {
        // given
        assertThrows(IllegalArgumentException.class, () ->
        {
            // when
            final String someObject = null;
            Assert.assertNotNull(String.class, someObject);
        });
    }

    @Test
    public void assertGreaterThanZero()
    {
        // given
        assertThrows(IllegalArgumentException.class, () ->
        {
            // when
            final int factor = 0;
            final int value  = 0;
            final String argumentName = "test";
            Assert.assertGreaterThan(factor, value, argumentName);
        });
    }

    @Test
    public void assertGreaterThanNegative()
    {
        assertThrows(IllegalArgumentException.class, () ->
        {
            // when
            final int factor = 0;
            final int value  = -1;
            final String argumentName = "test";
            Assert.assertGreaterThan(factor, value, argumentName);
        });
    }

    @Test
    public void assertGreaterThanPositive()
    {
        // when
        final int factor = 0;
        final int value  = 1;
        final String argumentName = "test";
        Assert.assertGreaterThan(factor, value, argumentName);
    }
}