package pl.homeportal.commons.assertions;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AssertTest
{
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void assertNotNull()
    {
        // given
        expectedException.expect(IllegalArgumentException.class);

        // when
        final String someObject = null;
        Assert.assertNotNull(String.class, someObject);
    }

    @Test
    public void assertGreaterThanZero()
    {
        // given
        expectedException.expect(IllegalArgumentException.class);

        // when
        final int factor = 0;
        final int value  = 0;
        final String argumentName = "test";
        Assert.assertGreaterThan(factor, value, argumentName);
    }

    @Test
    public void assertGreaterThanNegative()
    {
        expectedException.expect(IllegalArgumentException.class);

        // when
        final int factor = 0;
        final int value  = -1;
        final String argumentName = "test";
        Assert.assertGreaterThan(factor, value, argumentName);
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