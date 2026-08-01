package pl.homeportal.commons.data;

import pl.homeportal.commons.data.search.SortSpec;

import java.util.List;

/**
 * Zrodlo deklaracji sortowania. Zwraca wlasny typ {@link SortSpec}, a nie
 * {@code org.apache.lucene.search.SortField} — dzieki temu konsument nie kompiluje
 * sie przeciw Lucene tylko po to, zeby posortowac wyniki.
 */
public interface SortFieldAware
{
    List<SortSpec> getSortSpecs();
}
