package pl.homeportal.commons.data.repository;

import org.apache.lucene.analysis.Analyzer;
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
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import pl.homeportal.commons.data.SortFieldAware;
import pl.homeportal.commons.data.entity.AbstractEntity;
import pl.homeportal.commons.data.search.SearchQuery;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Transactional
public class FullTextRepositoryImpl<T extends AbstractEntity, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements FullTextRepository<T, ID>
{
    public static final String SEARCH_QUERY_CANNOT_BE_NULL = "SearchQuery cannot be null!";

    private static final int BATCH_SIZE_TO_LOAD_OBJECTS = 20;
    private static final int THREADS_TO_LOAD_OBJECTS = 100;

    private static final String ID   = "id";

    private final Class<T> domainClass;
    private final EntityManager entityManager;

    public FullTextRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager)
    {
        super(entityInformation, entityManager);
        this.domainClass = entityInformation.getJavaType();
        this.entityManager = entityManager;
    }

    public FullTextRepositoryImpl(Class<T> domainClass, EntityManager entityManager)
    {
        super(domainClass, entityManager);
        this.domainClass = domainClass;
        this.entityManager = entityManager;
    }

    @Override
    public <S extends T> S save(S t)
    {
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager();
        t = super.save(t);
        fullTextEntityManager.index(t);
        fullTextEntityManager.flushToIndexes();

        return t;
    }

    @Override
    public void delete(T t)
    {
        super.delete(t);
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager();
        fullTextEntityManager.purge(domainClass, t.getId());
        fullTextEntityManager.flushToIndexes();
    }

    @Override
    public void deleteAll()
    {
        super.deleteAll();
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager();
        fullTextEntityManager.purgeAll(domainClass);
        fullTextEntityManager.flushToIndexes();
    }

    @Override
    public FullTextQuery createQuery(String queryString, SortField [] sortFields)
    {
        try
        {
            QueryParser parser = new QueryParser(ID, getAnalyzer());
            parser.setLowercaseExpandedTerms(true);
            Query luceneQuery = parser.parse(queryString);
            FullTextEntityManager fullTextEntityManager = getFullTextEntityManager();
            FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, domainClass);
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

    @Override
    public int countBySearchQuery(SearchQuery sQuery)
    {
        Assert.notNull(sQuery, SEARCH_QUERY_CANNOT_BE_NULL);
        if ( sQuery.isQueryEmpty() )
        {
            return new Long(count()).intValue();
        }

        return createQuery(sQuery.getQueryString(), null).getResultSize();
    }

    @Override
    public List<T> findAllBySearchQuery(SearchQuery sQuery)
    {
        Assert.notNull(sQuery, SEARCH_QUERY_CANNOT_BE_NULL);
        if(sQuery.isQueryEmpty())
        {
            return findAll(createPageable(sQuery)).getContent();
        }

        FullTextQuery query = createQuery(sQuery.getQueryString(), getDefaultSortFields(sQuery));
        query.setMaxResults(sQuery.getPageSize());
        query.setFirstResult(sQuery.getPageNumber() * sQuery.getPageSize());
        List<T> list = query.getResultList();

        return list;
    }

    @Override
    public void indexAll()
    {
        try
        {
            getFullTextEntityManager()
            .createIndexer(domainClass)
            .batchSizeToLoadObjects(BATCH_SIZE_TO_LOAD_OBJECTS)
            .threadsToLoadObjects(THREADS_TO_LOAD_OBJECTS)
            .cacheMode(CacheMode.NORMAL)
            .optimizeOnFinish(true)
            .startAndWait();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("Indexing interrupted", e);
        }
    }

    @Override
    public void indexAll(int batchSize, int threads)
    {
        try
        {
            getFullTextEntityManager()
            .createIndexer(domainClass)
            .batchSizeToLoadObjects(batchSize)
            .threadsToLoadObjects(threads)
            .cacheMode(CacheMode.NORMAL)
            .optimizeOnFinish(true)
            .startAndWait();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("Indexing interrupted", e);
        }
    }

    @Override
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

    protected SortField [] getDefaultSortFields(SortFieldAware query)
    {
        return query.getSortFields().toArray(new SortField[query.getSortFields().size()]);
    }

    private FullTextEntityManager getFullTextEntityManager()
    {
        return Search.getFullTextEntityManager(entityManager);
    }

    private Analyzer getAnalyzer()
    {
        return new PerFieldAnalyzerWrapper(new StandardAnalyzer());
    }

    private Pageable createPageable(SearchQuery sQuery)
    {
        final List<Sort.Order> orders = new ArrayList<>();
        LinkedList<SortField> sortFields = sQuery.getSortFields();
        for (SortField sortField : sortFields)
        {
            if(sortField.getReverse())
            {
                orders.add(Sort.Order.desc(sortField.getField()));
            }
            orders.add(Sort.Order.asc(sortField.getField()));
        }
        return new PageRequest(sQuery.getPageNumber(), sQuery.getPageSize(), Sort.by(orders));
    }

}
