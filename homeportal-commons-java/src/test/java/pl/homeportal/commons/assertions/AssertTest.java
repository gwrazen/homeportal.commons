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
        expectedException.expect(NullPointerException.class);

        final String someObject = null;
        Assert.assertNotNull(String.class, someObject);
    }
}