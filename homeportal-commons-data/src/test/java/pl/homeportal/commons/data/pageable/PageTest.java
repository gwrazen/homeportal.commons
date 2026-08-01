package pl.homeportal.commons.data.pageable;

import org.junit.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PageTest
{
    /**
     * Regresja: Page dziedziczyl po PageRequest skonstruowanym na stale jako (1, 20),
     * a wlasne pola tylko go przeslanialy. Odziedziczone equals/hashCode czytaly stan
     * nadklasy, wiec dwa formularze rozniace sie numerem strony byly **rowne** —
     * co trulo kazdy cache kluczowany pageable'em.
     */
    @Test
    public void pagesDifferingByNumberAreNotEqual()
    {
        final Page first = new Page(1, 20);
        final Page second = new Page(2, 20);

        assertNotEquals(first, second);
        assertNotEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void pagesWithTheSameStateAreEqual()
    {
        assertEquals(new Page(3, 50), new Page(3, 50));
    }

    /**
     * Regresja: odziedziczone getOffset() liczylo na stanie nadklasy, wiec zwracalo
     * stale 20 niezaleznie od numeru strony.
     */
    @Test
    public void offsetFollowsThePageNumber()
    {
        assertEquals(0L, new Page(1, 20).getOffset());
        assertEquals(20L, new Page(2, 20).getOffset());
        assertEquals(80L, new Page(5, 20).getOffset());
    }

    /**
     * Numeracja formularza jest 1-based, kontrakt Spring Data — 0-based.
     * toPageable() jest jedynym miejscem, gdzie ta konwersja zyje.
     */
    @Test
    public void toPageableConvertsToSpringDataNumbering()
    {
        final Pageable pageable = new Page(3, 20).toPageable();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    public void navigationKeepsOneBasedNumbering()
    {
        final Page page = new Page(2, 20);

        assertEquals(3, page.next().getPageNumber());
        assertEquals(1, page.previousOrFirst().getPageNumber());
        assertEquals(1, page.first().getPageNumber());
        assertTrue(page.hasPrevious());
        assertFalse(new Page(1, 20).hasPrevious());
    }

    /**
     * Regresja: konstruktor (page, size) przekazywal do nadklasy Sort.unsorted(),
     * ale getSort() i tak zwracalo domyslne sortowanie po "added" — dwa zrodla
     * prawdy o tym samym.
     */
    @Test
    public void sortIsConsistentWithTheDeclaredField()
    {
        final Page defaults = new Page();
        assertEquals("added", defaults.getSortField());
        assertTrue(defaults.isReverseOrder());
        assertEquals(Sort.by("added").descending(), defaults.getSort());

        final Page ascending = new Page(1, 20, Sort.by("price").ascending());
        assertEquals("price", ascending.getSortField());
        assertFalse(ascending.isReverseOrder());
        assertEquals(Sort.by("price").ascending(), ascending.getSort());
    }
}
