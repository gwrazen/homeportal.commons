package pl.homeportal.commons.data.search.encoding;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;

import java.math.BigInteger;
import java.util.Date;

import static org.apache.commons.lang3.StringUtils.stripAccents;
import static pl.homeportal.commons.text.Constants.EMPTY_STRING;
import static pl.homeportal.commons.text.Constants.REGEXP_SPACE;
import static pl.homeportal.commons.text.Constants.SPACE;

/**
 * Zestaw kodowan uzywanych przez indeks i zapytania. Kazdy {@code QueryParameter}
 * deklaruje, ktorego z nich uzywa — patrz {@code QueryParameter#encoder()}.
 */
public final class ValueEncoders
{
    /**
     * Tekst bez spacji, bez znakow diakrytycznych, malymi literami.
     * Odpowiada {@code PropertyTypeBridge} — pola typu miasto, dzielnica, ulica.
     */
    public static final ValueEncoder TEXT = value -> {
        if (value == null)
        {
            return null;
        }

        String text = String.valueOf(value).replaceAll(REGEXP_SPACE, EMPTY_STRING);
        text = stripAccents(text);

        return text.toLowerCase();
    };

    /**
     * Wartosc cechy: malymi literami, bez znakow specjalnych, ze **zachowanymi**
     * znakami diakrytycznymi. Odpowiada {@code FeatureBridge}.
     *
     * To wlasnie tu byl rozjazd: indeks zapisywal "wtórny", a zapytanie szukalo
     * "wtorny", wiec filtr po cesze z polskim znakiem nie zwracal nigdy nic.
     */
    public static final ValueEncoder FEATURE = value -> {
        if (value == null)
        {
            return null;
        }

        final String encoded = String.valueOf(value)
                .toLowerCase()
                .replaceAll(FeatureEncoding.SPECIAL_CHARACTERS, EMPTY_STRING)
                .replaceAll(FeatureEncoding.MORE_THAN_ONE_SPACE, SPACE)
                .trim();

        return encoded.isEmpty() ? null : encoded;
    };

    /**
     * Liczba w postaci sortowalnej leksykograficznie: wartosc przesunieta o 2^63
     * i uzupelniona zerami do stalej szerokosci.
     *
     * Przesuniecie jest konieczne, bo samo uzupelnianie zerami psuje porzadek
     * liczb ujemnych ("00000000-5" sortuje sie przed "00000000-9"). Stala szerokosc
     * pokrywa caly zakres {@code long} — wczesniejsze {@code intValue()} zawijalo
     * wartosci powyzej {@code Integer.MAX_VALUE} na ujemne (cena 3 mld indeksowala
     * sie jako -1294967296).
     *
     * Czesc ulamkowa jest obcinana: zapytania zakresowe operuja na liczbach
     * calkowitych, wiec indeks musi byc z nimi spojny.
     */
    public static final ValueEncoder NUMERIC = value -> {
        if (value == null)
        {
            return null;
        }

        final Long number = toLong(value);
        if (number == null)
        {
            return null;
        }

        final String shifted = BigInteger.valueOf(number).add(NumericEncoding.OFFSET).toString();
        final StringBuilder padded = new StringBuilder();
        for (int i = shifted.length(); i < NumericEncoding.WIDTH; i++)
        {
            padded.append('0');
        }

        return padded.append(shifted).toString();
    };

    /**
     * Data z dokladnoscia do sekundy, w formacie {@code DateTools} (yyyyMMddHHmmss).
     */
    public static final ValueEncoder DATE = value -> {
        if (!(value instanceof Date))
        {
            return null;
        }

        return DateTools.dateToString((Date) value, Resolution.SECOND);
    };

    private ValueEncoders()
    {
    }

    private static Long toLong(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }

        try
        {
            return Long.valueOf(String.valueOf(value).trim());
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    static final class NumericEncoding
    {
        static final BigInteger OFFSET = BigInteger.valueOf(2).pow(63);
        static final int WIDTH = 20;
    }

    static final class FeatureEncoding
    {
        static final String SPECIAL_CHARACTERS = "[^\\p{L}\\p{Nd}\\s]+";
        static final String MORE_THAN_ONE_SPACE = "\\s{2,}";
    }
}
