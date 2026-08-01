package pl.homeportal.commons.data.search;

import org.junit.Test;
import pl.homeportal.commons.data.search.bridge.DateBridge;
import pl.homeportal.commons.data.search.bridge.FeatureBridge;
import pl.homeportal.commons.data.search.bridge.NumericBridge;
import pl.homeportal.commons.data.search.bridge.PropertyTypeBridge;
import pl.homeportal.commons.data.search.encoding.ValueEncoder;
import pl.homeportal.commons.data.search.encoding.ValueEncoders;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Kontrakt: dla kazdej wartosci brzegowej strona indeksu (bridge) i strona zapytania
 * (encoder zadeklarowany przy QueryParameter) musza dawac ten sam wynik. Rozjazd
 * miedzy nimi nie daje zadnego bledu — tylko ciche zero wynikow — wiec jedyne, co
 * go pilnuje, to ten test.
 */
public class EncodingContractTest
{
    private static final List<String> EDGE_CASES = Arrays.asList(
            "Nowy Sącz",          // nazwa dwuczlonowa z polskim znakiem
            "wtórny",             // wartosc cechy z polskim znakiem
            "wolnostojący",
            "Świętokrzyskie",
            "  spacje  wokol  ",
            "MiXeD CaSe",
            "myslnik-i_podkreslnik",
            "1234");

    @Test
    public void textEncoderMatchesPropertyTypeBridge()
    {
        final PropertyTypeBridge bridge = new PropertyTypeBridge();
        for (String value : EDGE_CASES)
        {
            assertEquals("Rozjazd kodowania dla: '" + value + "'",
                         bridge.objectToString(value),
                         ValueEncoders.TEXT.encode(value));
        }
    }

    @Test
    public void featureEncoderMatchesFeatureBridge()
    {
        final FeatureBridge bridge = new FeatureBridge();
        for (String value : EDGE_CASES)
        {
            // bridge dostaje caly blob |NAZWA:wartosc|, encoder — sama wartosc
            final String indexed = bridge.objectToString("|MARKET:" + value + "|");
            final String queried = ValueEncoders.FEATURE.encode(value);

            assertEquals("Rozjazd kodowania cechy dla: '" + value + "'", indexed, queried);
        }
    }

    /**
     * Regresja: FeatureBridge zachowywal znaki diakrytyczne, a zapytanie strippowalo
     * je przez PropertyTypeBridge — filtr po cesze "wtórny" nie zwracal nigdy nic.
     */
    @Test
    public void featureEncodingKeepsDiacritics()
    {
        assertEquals("wtórny", ValueEncoders.FEATURE.encode("wtórny"));
        assertEquals("wtorny", ValueEncoders.TEXT.encode("wtórny"));
    }

    @Test
    public void numericEncoderMatchesNumericBridge()
    {
        final NumericBridge bridge = new NumericBridge();
        for (long value : new long[]{0L, 1L, -1L, 120000L, Integer.MAX_VALUE, 3_000_000_000L, Long.MIN_VALUE, Long.MAX_VALUE})
        {
            assertEquals("Rozjazd kodowania liczby: " + value,
                         bridge.objectToString(value),
                         ValueEncoders.NUMERIC.encode(String.valueOf(value)));
        }
    }

    @Test
    public void dateEncoderMatchesDateBridge()
    {
        final Date date = new Date(1_754_000_000_000L);

        assertEquals(new DateBridge().objectToString(date), ValueEncoders.DATE.encode(date));
    }

    /**
     * Regresja: intValue() zawijalo wartosci powyzej Integer.MAX_VALUE na ujemne —
     * cena 3 mld indeksowala sie jako "-1294967296" i nie trafiala w zaden zakres.
     */
    @Test
    public void numericEncodingSurvivesValuesAboveIntegerRange()
    {
        final String encoded = ValueEncoders.NUMERIC.encode(3_000_000_000L);

        assertNotNull(encoded);
        assertTrue("Kodowanie musi zachowac porzadek", encoded.compareTo(ValueEncoders.NUMERIC.encode(2_000_000_000L)) > 0);
    }

    /**
     * Regresja: uzupelnianie zerami psulo porzadek liczb ujemnych —
     * pad("-5") = "00000000-5" sortowalo sie przed pad("-9") = "00000000-9".
     */
    @Test
    public void numericEncodingKeepsOrderOfNegativeValues()
    {
        final String minusNine = ValueEncoders.NUMERIC.encode(-9);
        final String minusFive = ValueEncoders.NUMERIC.encode(-5);
        final String zero = ValueEncoders.NUMERIC.encode(0);

        assertTrue("-9 < -5", minusNine.compareTo(minusFive) < 0);
        assertTrue("-5 < 0", minusFive.compareTo(zero) < 0);
    }

    @Test
    public void numericEncodingHasConstantWidth()
    {
        final int width = ValueEncoders.NUMERIC.encode(1).length();

        assertEquals(width, ValueEncoders.NUMERIC.encode(Long.MAX_VALUE).length());
        assertEquals(width, ValueEncoders.NUMERIC.encode(Long.MIN_VALUE).length());
        assertEquals(width, ValueEncoders.NUMERIC.encode(-1).length());
    }

    @Test
    public void encodersAreNullSafe()
    {
        for (ValueEncoder encoder : Arrays.asList(ValueEncoders.TEXT, ValueEncoders.FEATURE,
                                                  ValueEncoders.NUMERIC, ValueEncoders.DATE,
                                                  ValueEncoders.ANALYZED))
        {
            assertNull(encoder.encode(null));
        }
    }

    /**
     * Pole bez bridge'a trafia do indeksu jako ciag tokenow, wiec wartosc zapytania musi dojsc
     * do QueryParsera nietknieta. TEXT skleilby fraze w term, ktorego w indeksie nie ma.
     */
    @Test
    public void analyzedEncoderLeavesValueForTheQueryParser()
    {
        assertEquals("mieszkanie Bielsko Biała", ValueEncoders.ANALYZED.encode("  mieszkanie Bielsko Biała  "));
        assertEquals("mieszkaniebielskobiala", ValueEncoders.TEXT.encode("mieszkanie Bielsko Biała"));
        assertNull(ValueEncoders.ANALYZED.encode("   "));
    }

    @Test
    public void numericEncoderRejectsNonNumericInput()
    {
        assertNull(ValueEncoders.NUMERIC.encode("nie-liczba"));
    }
}
