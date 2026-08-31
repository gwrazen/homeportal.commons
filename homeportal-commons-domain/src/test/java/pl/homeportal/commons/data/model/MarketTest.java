package pl.homeportal.commons.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Konwersje rynku muszą przyjmować OBIE postacie — nazwę i identyfikator.
 *
 * <p>Powód jest konkretny, nie teoretyczny: kreator ofert zapisuje cechy surową wartością
 * z formularza, a {@code <select>} wysyła identyfikator. Do 2026-08-31 {@code asId} rozumiał
 * wyłącznie nazwy, więc {@code Offer.getMarket()} oddawał {@code null} dla 96% ofert 2026Q3 —
 * rynek był w bazie i był niewidoczny dla wyceny HAC i dla wyszukiwarki.
 */
public class MarketTest
{
    @Test
    public void asIdAcceptsNames()
    {
        assertEquals(Market.PRIMARY, Market.asId("pierwotny"));
        assertEquals(Market.SECONDARY, Market.asId("wtórny"));
    }

    /** Sedno naprawy: identyfikator na wejściu ma wrócić, a nie zniknąć jako „nie wiem". */
    @Test
    public void asIdAcceptsIdsAlready()
    {
        assertEquals(Market.PRIMARY, Market.asId("1"));
        assertEquals(Market.SECONDARY, Market.asId("2"));
    }

    /** Wielokrotne wywołanie nie może niczego psuć — kod woła to na wejściu i przy odczycie. */
    @Test
    public void asIdIsIdempotent()
    {
        assertEquals(Market.SECONDARY, Market.asId(Market.asId(Market.asId("wtórny"))));
    }

    @Test
    public void asStringAcceptsBothForms()
    {
        assertEquals(Market.PRIMARY_STRING, Market.asString("1"));
        assertEquals(Market.SECONDARY_STRING, Market.asString("2"));
        assertEquals(Market.SECONDARY_STRING, Market.asString("wtórny"));
        assertEquals(Market.PRIMARY_STRING, Market.asString(Market.asString("1")));
    }

    /** Brak danych zostaje brakiem danych — nie zamieniamy nieznanego w twierdzenie. */
    @Test
    public void unknownStaysNull()
    {
        assertNull(Market.asId(null));
        assertNull(Market.asId(""));
        assertNull(Market.asId("3"));
        assertNull(Market.asId("cokolwiek"));
        assertNull(Market.asString("7"));
    }
}
