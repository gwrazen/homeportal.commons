package pl.homeportal.commons.data.search;

import pl.homeportal.commons.data.search.encoding.ValueEncoder;
import pl.homeportal.commons.data.search.encoding.ValueEncoders;

/**
 * Parametr wyszukiwania: nazwa pola w indeksie plus deklaracja, jak koduje sie
 * jego wartosc.
 *
 * Deklaracja kodowania **musi** odpowiadac bridge'owi uzytemu na tym polu w encji.
 * Do 5.0 strona zapytania kodowala kazdy term przez PropertyTypeBridge niezaleznie
 * od tego, czym pole bylo zaindeksowane — stad ciche zero wynikow dla cech
 * z polskimi znakami i dla pol bez tego bridge'a (np. nazwy dwuczlonowe miast).
 *
 * Domyslne {@link ValueEncoders#TEXT} zachowuje dotychczasowe zachowanie, wiec
 * istniejace enumy konsumentow kompiluja sie bez zmian — ale kazdy parametr
 * wskazujacy na pole z innym bridge'em nalezy nadpisac, np.:
 *
 * <pre>
 * FEATURES("features") {
 *     public ValueEncoder encoder() { return ValueEncoders.FEATURE; }
 * },
 * PRICE("price") {
 *     public ValueEncoder encoder() { return ValueEncoders.NUMERIC; }
 * }
 * </pre>
 */
public interface QueryParameter
{
    String getValue();

    QueryParameter getByValue(String value);

    default ValueEncoder encoder()
    {
        return ValueEncoders.TEXT;
    }
}
