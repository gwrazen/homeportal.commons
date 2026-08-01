package pl.homeportal.commons.data.search.encoding;

/**
 * Kodowanie wartosci do postaci zapisanej w indeksie Lucene.
 *
 * Ten sam encoder obowiazuje po obu stronach: bridge uzywa go przy indeksowaniu,
 * a {@code SearchQuery} przy budowaniu zapytania. Wczesniej byly to dwie niezalezne
 * sciezki — indeks wybieral bridge adnotacja na polu encji, a zapytanie kodowalo
 * na sztywno {@code PropertyTypeBridge}em. Rozjazd nie dawal zadnego bledu, tylko
 * ciche zero wynikow (np. dla cech z polskimi znakami albo nazw dwuczlonowych).
 *
 * Implementacje musza byc bezstanowe i deterministyczne.
 */
public interface ValueEncoder
{
    /**
     * @return wartosc w postaci indeksowanej albo {@code null}, gdy wejscie jest puste
     *         lub nie da sie go zakodowac
     */
    String encode(Object value);
}
