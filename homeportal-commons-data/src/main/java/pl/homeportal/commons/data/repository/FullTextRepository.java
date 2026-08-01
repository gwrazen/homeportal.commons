package pl.homeportal.commons.data.repository;

import org.springframework.data.domain.Pageable;
import pl.homeportal.commons.data.search.SearchQuery;

import java.util.List;

/**
 * Fragment repozytorium Spring Data dokladajacy operacje na indeksie Lucene.
 *
 * Zmiany w 6.0:
 * - {@code save}/{@code delete} zostaly przemianowane na {@code indexedSave}/{@code indexedDelete}.
 *   Poprzednie nazwy mialy te sama erased signature co {@code CrudRepository} i **wygrywaly**
 *   rozstrzygniecie fragmentu, przez co {@code repository.save(nowaEncja)} wolalo merge zamiast
 *   persist i zostawialo przekazany obiekt bez identyfikatora. Po zmianie {@code save} i
 *   {@code delete} znowu znacza to, co w Spring Data.
 * - z API zniknely typy {@code org.apache.lucene.*} i {@code org.hibernate.search.*}; sortowanie
 *   opisuje wlasny {@code SortSpec}, a budowanie zapytania jest szczegolem implementacji.
 *   Dzieki temu repozytoria konsumentow nie kompiluja sie juz przeciw Lucene 5, co odblokowuje
 *   pozniejsza migracje na Hibernate Search 6.
 */
public interface FullTextRepository<T>
{
    /** Zapis encji wraz z aktualizacja indeksu. */
    <S extends T> S indexedSave(S t);

    /** Usuniecie encji wraz z usunieciem jej dokumentu z indeksu. */
    void indexedDelete(T t);

    void deleteAll(Class<T> t);

    void purge(T t);

    void indexAll(Class<T> t);

    void indexAll(int batchSize, int threads, Class<T> t);

    void indexOne(T entity);

    void optimizeIndex();

    long count(Class<T> t);

    long countByIndex(Class<T> t);

    int countBySearchQuery(SearchQuery query, Class<T> t);

    List<T> findAll(Pageable pageable, Class<T> t);

    List<T> findAllBySearchQuery(SearchQuery searchQuery, Class<T> t);
}
