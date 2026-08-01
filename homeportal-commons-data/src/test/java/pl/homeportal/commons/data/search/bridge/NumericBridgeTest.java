package pl.homeportal.commons.data.search.bridge;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Format zapisu zmienil sie w 6.0: wartosc jest przesuwana o 2^63 i uzupelniana
 * zerami do stalej szerokosci 20 znakow. Poprzedni format ("0000120000") obcinal
 * do int i psul porzadek liczb ujemnych — indeks sprzed 6.0 wymaga reindeksu.
 *
 * Created by gwrazen on 24/08/2015.
 */
public class NumericBridgeTest
{
    private NumericBridge numericBridge = new NumericBridge();

    @Test
    public void testObjectToString()
    {
        assertEquals("09223372036854895808", numericBridge.objectToString(120000d));
    }

    @Test
    public void keepsNumericOrderAsLexicographicOrder()
    {
        final String small = numericBridge.objectToString(120000);
        final String big = numericBridge.objectToString(3_000_000_000L);

        assertTrue(small.compareTo(big) < 0);
    }

    @Test
    public void truncatesFractionToMatchIntegerRangeQueries()
    {
        assertEquals(numericBridge.objectToString(45), numericBridge.objectToString(45.9d));
    }

    @Test
    public void stringToObjectParsesPlainNumbers()
    {
        assertEquals(Long.valueOf(120000), numericBridge.stringToObject("120000"));
        assertNull(numericBridge.stringToObject("nie-liczba"));
    }

    @Test
    public void nullSafe()
    {
        assertNull(numericBridge.objectToString(null));
    }
}
