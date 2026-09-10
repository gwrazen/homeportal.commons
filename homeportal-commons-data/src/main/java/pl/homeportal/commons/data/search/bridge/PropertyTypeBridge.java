package pl.homeportal.commons.data.search.bridge;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;
import pl.homeportal.commons.data.search.encoding.ValueEncoders;

/**
 * Cienki adapter na {@link ValueEncoders#TEXT} — to samo kodowanie stosuje strona
 * zapytania, wiec indeks i zapytanie nie moga sie rozjechac.
 */
public class PropertyTypeBridge implements ValueBridge<Object, String>
{
    @Override
    public String toIndexedValue(Object object, ValueBridgeToIndexedValueContext context)
    {
        return ValueEncoders.TEXT.encode(object);
    }
}
