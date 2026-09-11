package pl.homeportal.commons.data.search.bridge;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

/**
 * Zastepuje wbudowany mostek Search 5, ktorego 6/7 juz nie ma.
 *
 * ⚠️ NIE zastapiony natywnym mapowaniem: {@code @GenericField} zapisalby pole numerycznie albo
 * po swojemu, a strona zapytania sklada tekstowe zakresy i dopasowania Lucene'a. Format zapisu
 * musi zostac znak w znak, inaczej indeks przestaje pasowac do zapytan — bez bledu, samym zerem.
 */
public class EnumAsStringBridge implements ValueBridge<Object, String>
{
    @Override
    public String toIndexedValue(Object value, ValueBridgeToIndexedValueContext context)
    {
        return value == null ? null : ((Enum<?>) value).name();
    }
}
