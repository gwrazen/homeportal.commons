package pl.homeportal.commons.data.search.bridge;

import org.hibernate.search.bridge.StringBridge;
import pl.homeportal.commons.data.search.encoding.ValueEncoders;

/**
 * Cienki adapter na {@link ValueEncoders#TEXT} — to samo kodowanie stosuje strona
 * zapytania, wiec indeks i zapytanie nie moga sie rozjechac.
 */
public class PropertyTypeBridge implements StringBridge
{
    @Override
    public String objectToString(Object object)
    {
        return ValueEncoders.TEXT.encode(object);
    }
}
