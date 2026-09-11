package pl.homeportal.commons.data.search.bridge;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

/**
 * Zapisuje identyfikator encji jako zwykly tekst — dokladnie tak, jak robil to domyslny
 * mostek {@code @DocumentId} w Search 5.
 *
 * ⚠️ Nie {@link NumericBridge}: tamten koduje liczby z przesunieciem i stala szerokoscia
 * pod zakresy, wiec {@code id:5} nie trafiloby w nic.
 */
public class IdentifierAsStringBridge implements ValueBridge<Object, String>
{
    @Override
    public String toIndexedValue(Object value, ValueBridgeToIndexedValueContext context)
    {
        return value == null ? null : String.valueOf(value);
    }
}
