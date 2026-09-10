package pl.homeportal.commons.data.search.bridge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals("09223372036854895808", numericBridge.toIndexedValue(120000d, null));
    }

    @Test
    public void keepsNumericOrderAsLexicographicOrder()
    {
        final String small = numericBridge.toIndexedValue(120000, null);
        final String big = numericBridge.toIndexedValue(3_000_000_000L, null);

        assertTrue(small.compareTo(big) < 0);
    }

    @Test
    public void truncatesFractionToMatchIntegerRangeQueries()
    {
        assertEquals(numericBridge.toIndexedValue(45, null), numericBridge.toIndexedValue(45.9d, null));
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
        assertNull(numericBridge.toIndexedValue(null, null));
    }
}
