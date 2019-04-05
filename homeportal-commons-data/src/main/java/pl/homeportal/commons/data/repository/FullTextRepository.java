package pl.homeportal.commons.data.repository;

import org.apache.lucene.search.SortField;
import org.hibernate.search.jpa.FullTextQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import pl.homeportal.commons.data.search.SearchQuery;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface FullTextRepository<T, ID extends Serializable> extends JpaRepository<T, ID>
{
    FullTextQuery createQuery(String queryString, SortField[] sortFields);

    int countBySearchQuery(SearchQuery query);

    List<T> findAll(int page, int maxQty);

    List<T> findAllAndSort(int currentPage, int maxResults, String sort, boolean reverse);

    List<T> findAllBySearchQuery(SearchQuery searchQuery);

    List<T> findAllBySearchQuery(SearchQuery searchQuery, int page, int maxQty);

    void indexAll();

    void indexAll(int batchSize, int threads);

    void indexOne(T entity);

    void optimizeIndex();
}
