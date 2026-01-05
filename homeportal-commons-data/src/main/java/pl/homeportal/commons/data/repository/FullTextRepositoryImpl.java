package pl.homeportal.commons.data.repository;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.hibernate.CacheMode;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import pl.homeportal.commons.data.SortFieldAware;
import pl.homeportal.commons.data.entity.AbstractEntity;
import pl.homeportal.commons.data.search.SearchQuery;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static pl.homeportal.commons.text.Constants.SPACE;


public class FullTextRepositoryImpl<T extends AbstractEntity> implements FullTextRepository<T>
{
    public static final String SEARCH_QUERY_CANNOT_BE_NULL = "SearchQuery cannot be null!";
    public static final String DOCUMENTS_COUNT = "(id:[0 TO 999999999])";

    private static final int BATCH_SIZE_TO_LOAD_OBJECTS = 100;
    private static final int THREADS_TO_LOAD_OBJECTS = 10;
    private static final String ID = "id";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <S extends T> S save(S t)
    {
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager();
        t = fullTextEntityManager.merge(t);
        fullTextEntityManager.index(t);
        fullTextEntityManager.flushToIndexes();

        return t;
    }

    @Override
    public void delete(T t)
    {
        FullTextEntityManager entityManager = getFullTextEntityManager();
        entityManager.remove(t);
        entityManager.purge(t.getClass(), t.getId());
        entityManager.flushToIndexes();
    }

    @Override
    public void deleteAll(Class<T> t)
    {
        FullTextEntityManager entityManager = getFullTextEntityManager();
        for (T one : findAll(t))
        {
            entityManager.remove(one);
        }
        entityManager.purgeAll(t);
        entityManager.flushToIndexes();
    }

    @Override
    public void purge(T t)
    {
        FullTextEntityManager entityManager = getFullTextEntityManager();
        entityManager.purge(t.getClass(), t.getId());
        entityManager.flushToIndexes();
    }

    @Override
    public long count(Class<T> t)
    {
        return (Long) entityManager.createQuery("select count(t) from " + t.getSimpleName() + " t where t.id > 0").getSingleResult();
    }

    @Override
    public long countByIndex(Class<T> t)
    {
        try
        {
            return createQuery(DOCUMENTS_COUNT, null, t).getResultSize();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int countBySearchQuery(SearchQuery sQuery, Class<T> t)
    {
        Assert.notNull(sQuery, SEARCH_QUERY_CANNOT_BE_NULL);
        if (sQuery.isQueryEmpty())
        {
            return new Long(count(t)).intValue();
        }

        return createQuery(sQuery.getQueryString(), null, t).getResultSize();
    }

    public List<T> findAll(Class<T> t)
    {
        return entityManager.createQuery("select t from " + t.getSimpleName() + " t").getResultList();
    }

    @Override
    public List<T> findAll(Pageable pageable, Class<T> t)
    {
        final String stringQuery = new StringBuffer()
                .append("select t from ")
                .append(t.getSimpleName())
                .append(" t ")
                .append("order by ")
                .append(getSort(pageable.getSort()))
                .toString();

        javax.persistence.Query query = entityManager.createQuery(stringQuery);
        query.setMaxResults(pageable.getPageSize());
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        return query.getResultList();
    }

    @Override
    public List<T> findAllBySearchQuery(SearchQuery sQuery, Class<T> t)
    {
        Assert.notNull(sQuery, SEARCH_QUERY_CANNOT_BE_NULL);
        if (sQuery.isQueryEmpty())
        {
            return findAll(createPageable(sQuery), t);
        }

        FullTextQuery query = createQuery(sQuery.getQueryString(), getDefaultSortFields(sQuery), t);
        query.setMaxResults(sQuery.getPageSize());
        query.setFirstResult(sQuery.getPageNumber() * sQuery.getPageSize());
        List<T> list = query.getResultList();

        return list;
    }

    @Override
    public void indexAll(Class<T> t)
    {
        indexAll(BATCH_SIZE_TO_LOAD_OBJECTS, THREADS_TO_LOAD_OBJECTS, t);
    }

    @Override
    public void indexAll(int batchSize, int threads, Class<T> t)
    {
        try
        {
            getFullTextEntityManager()
                    .createIndexer(t)
                    .batchSizeToLoadObjects(batchSize)
                    .threadsToLoadObjects(threads)
                    .cacheMode(CacheMode.IGNORE)
                    .optimizeOnFinish(true)
                    .startAndWait();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("Indexing interrupted", e);
        }
    }

    @Override
    @Transactional
    public void indexOne(T entity)
    {
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager();
        entity = fullTextEntityManager.merge(entity);
        fullTextEntityManager.index(entity);
        fullTextEntityManager.flushToIndexes();
    }

    @Override
    public void optimizeIndex()
    {
        getFullTextEntityManager().getSearchFactory().optimize();
    }

    public SortField[] getDefaultSortFields(SortFieldAware query)
    {
        return query.getSortFields().toArray(new SortField[query.getSortFields().size()]);
    }

    @Override
    public FullTextQuery createQuery(String queryString, SortField[] sortFields, Class<T> t)
    {
        try
        {
            QueryParser parser = new QueryParser(ID, getAnalyzer(false));
            parser.setLowercaseExpandedTerms(true);
            Query luceneQuery = parser.parse(queryString);
            FullTextEntityManager fullTextEntityManager = getFullTextEntityManager();
            FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, t);
            if (sortFields != null && sortFields.length > 0)
            {
                fullTextQuery.setSort(new org.apache.lucene.search.Sort(sortFields));
            }
            return fullTextQuery;
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Probably parsing lucene query exception", e);
        }
    }

    private FullTextEntityManager getFullTextEntityManager()
    {
        return Search.getFullTextEntityManager(entityManager);
    }

    private Analyzer getAnalyzer(boolean keywordAna)
    {
//        return new PerFieldAnalyzerWrapper(new StandardAnalyzer());
        return new PerFieldAnalyzerWrapper(new KeywordAnalyzer());
    }

    private Pageable createPageable(SearchQuery sQuery)
    {
        final List<Sort.Order> orders = new ArrayList<>();
        LinkedList<SortField> sortFields = sQuery.getSortFields();
        for (SortField sortField : sortFields)
        {
            if (sortField.getReverse())
            {
                orders.add(Sort.Order.desc(sortField.getField()));
            }
            orders.add(Sort.Order.asc(sortField.getField()));
        }
        return PageRequest.of(sQuery.getPageNumber(), sQuery.getPageSize(), Sort.by(orders));
    }

    private String getSort(Sort sort)
    {
        for (Sort.Order order : sort)
        {
            return order.getProperty() + SPACE + order.getDirection().name();
        }
        return " id asc";
    }
}
