package pl.homeportal.commons.data.search.bridge;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

/**
 * Zapisuje wartosc logiczna jako {@code "true"} / {@code "false"}, czyli tak, jak robil to
 * domyslny mostek Search 5.
 *
 * ⚠️ {@code @GenericField} na {@code boolean} zapisuje w Search 6/7 pole NATYWNIE, a strona
 * zapytania sklada termy tekstowe klasycznym {@code QueryParser}-em Lucene'a ({@code partner:true}).
 * Term tekstowy nie trafia w pole natywne — zapytanie oddaje ZERO wynikow i nie zglasza bledu.
 * Zlapane na produkcji 2026-09-11: po wdrozeniu Boota 3 filtry w panelach agencji i partnerow
 * przestaly cokolwiek znajdowac, a strona renderowala sie normalnie z "Brak rezultatow".
 */
public class BooleanAsStringBridge implements ValueBridge<Boolean, String>
{
    @Override
    public String toIndexedValue(Boolean value, ValueBridgeToIndexedValueContext context)
    {
        return value == null ? null : value.toString();
    }
}
