package pl.homeportal.commons.data.search;

import org.junit.Test;
import pl.homeportal.commons.data.pageable.Page;

import static org.junit.Assert.assertEquals;

/**
 * Kontrakt przeliczenia strony: formularz jest 1-based, indeks liczy od zera.
 */
public class SearchQueryBuilderTest
{
    private final SearchQueryBuilder<Page> builder = new SearchQueryBuilder<Page>()
    {
        @Override
        protected SearchQuery build(Page form)
        {
            final SearchQuery query = new SearchQuery();
            setPageable(form, query);

            return query;
        }

        @Override
        protected void addSortField(Page form, SearchQuery query)
        {
        }
    };

    @Test
    public void firstPageBecomesZero()
    {
        assertEquals(0, builder.build(new Page(1, 20)).getPageNumber());
    }

    @Test
    public void furtherPagesShiftByOne()
    {
        assertEquals(2, builder.build(new Page(3, 50)).getPageNumber());
        assertEquals(50, builder.build(new Page(3, 50)).getPageSize());
    }

    /**
     * Regresja: numer strony przychodzi z zadania HTTP, wiec bywa zerem. Bez clampu schodzil
     * do -1 i konczyl sie "Page index must not be less than zero!" — mimo ze Page#getOffset()
     * i Page#toPageable() ten sam przypadek juz obslugiwaly.
     */
    @Test
    public void pageBelowFirstIsClampedInsteadOfGoingNegative()
    {
        assertEquals(0, builder.build(new Page(0, 20)).getPageNumber());
        assertEquals(0, builder.build(new Page(-5, 20)).getPageNumber());
    }
}
