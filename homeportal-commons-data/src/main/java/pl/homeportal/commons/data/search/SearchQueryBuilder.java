package pl.homeportal.commons.data.search;

import org.springframework.data.domain.Pageable;

/**
 * Created by Grzegorz Wrazen on 2019-04-23
 */
public abstract class SearchQueryBuilder<SR extends Pageable>
{
    /** Pierwsza strona w numeracji formularza (Page jest 1-based, inaczej niz Spring Data). */
    private static final int FIRST_PAGE = 1;

    protected abstract SearchQuery build(SR sRequest);

    protected abstract void addSortField(SR sRequest, SearchQuery sQuery);

    /**
     * Numer strony z formularza jest 1-based, indeks liczy od zera — stad odjecie jedynki.
     *
     * Clamp do pierwszej strony jest tu z tego samego powodu, co w {@code Page#getOffset()}
     * i {@code Page#toPageable()}: parametr przychodzi z zadania HTTP, wiec moze byc zerem
     * albo liczba ujemna. Bez niego "?page=0" schodzilo do strony -1 i konczylo sie
     * "Page index must not be less than zero!" — czyli wyjatkiem zamiast wynikow, i to
     * niespojnie z dwoma pozostalymi miejscami, ktore ten sam numer juz klampowaly.
     */
    protected void setPageable(SR sRequest, SearchQuery sQuery)
    {
        sQuery.setPageNumber(Math.max(sRequest.getPageNumber(), FIRST_PAGE) - FIRST_PAGE);
        sQuery.setPageSize(sRequest.getPageSize());
    }
}