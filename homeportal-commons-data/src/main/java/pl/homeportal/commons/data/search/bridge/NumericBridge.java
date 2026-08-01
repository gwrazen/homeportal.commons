package pl.homeportal.commons.data.search.bridge;

import org.hibernate.search.bridge.StringBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.homeportal.commons.data.search.encoding.ValueEncoders;

/**
 * Cienki adapter na {@link ValueEncoders#NUMERIC} — to samo kodowanie stosuje strona
 * zapytania przy zakresach.
 *
 * UWAGA: format zapisu zmienil sie w 6.0 (przesuniecie o 2^63, stala szerokosc 20),
 * wiec indeks zbudowany wczesniej nie pasuje do nowych zapytan — wymagany jest pelny
 * reindeks. Poprzedni format zawijal wartosci powyzej Integer.MAX_VALUE na ujemne
 * i psul porzadek liczb ujemnych.
 *
 * @author gwrazen
 */
public class NumericBridge implements StringBridge
{
    private static final Logger LOG = LoggerFactory.getLogger(NumericBridge.class);

    @Override
    public String objectToString(Object o)
    {
        final String encoded = ValueEncoders.NUMERIC.encode(o);
        if (encoded == null && o != null)
        {
            LOG.warn("Unable to convert from Number to String: {}", o);
        }

        return encoded;
    }

    public Long stringToObject(String value)
    {
        if (null != value)
        {
            try
            {
                return Long.parseLong(value);
            }
            catch (Exception e)
            {
                LOG.warn("Unable to convert from String to Number: {}", value);
            }
        }

        return null;
    }
}
