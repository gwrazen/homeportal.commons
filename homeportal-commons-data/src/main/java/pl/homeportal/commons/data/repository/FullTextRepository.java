package pl.homeportal.commons.data.repository;

import org.apache.lucene.search.SortField;
import org.hibernate.search.jpa.FullTextQuery;
import org.springframework.data.domain.Pageable;
import pl.homeportal.commons.data.search.SearchQuery;

import java.util.List;

public interface FullTextRepository<T>
{
    <S extends T> S save(S t);

    void delete(T t);

    void deleteAll(Class<T> t);

    long count(Class<T> t);

    int countBySearchQuery(SearchQuery query, Class<T> t);

    List<T> findAll(Pageable pageable, Class<T> t);

    List<T> findAllBySearchQuery(SearchQuery searchQuery, Class<T> t);

    void indexAll(Class<T> t);

    void indexAll(int batchSize, int threads, Class<T> t);

    void indexOne(T entity);

    void optimizeIndex();

    FullTextQuery createQuery(String queryString, SortField[] sortFields, Class<T> t);
}
