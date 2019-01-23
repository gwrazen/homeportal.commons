package pl.homeportal.commons.data.search.bridge;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by gwrazen on 24/08/2015.
 */
public class NumericBridgeTest
{
    private NumericBridge numericBridge = new NumericBridge();

    @Test
    public void testObjectToString()
    {
        String value = numericBridge.objectToString(new Double(120000));
        assertEquals("0000120000", value);
    }
}
