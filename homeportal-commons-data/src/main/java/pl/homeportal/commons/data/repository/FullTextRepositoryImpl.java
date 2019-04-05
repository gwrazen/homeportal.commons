package pl.homeportal.commons.data.repository;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.CacheMode;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import pl.homeportal.commons.data.SortFieldAware;
import pl.homeportal.commons.data.search.SearchQuery;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

import static pl.homeportal.commons.data.search.SearchQuery.isEmpty;

@Transactional
public class FullTextRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements FullTextRepository<T, ID>
{
    private static final int BATCH_SIZE_TO_LOAD_OBJECTS = 20;
    private static final int THREADS_TO_LOAD_OBJECTS = 100;

    private static final String ID = "id";
    private static final String DESC = "desc";
    private static final String ASC = "asc";

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
    public FullTextQuery createQuery(String queryString, SortField[] sortFields)
    {
        QueryParser parser = new QueryParser(ID, getAnalyzer());
        parser.setLowercaseExpandedTerms(true);
        Query luceneQuery;
        try {
            luceneQuery = parser.parse(queryString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Parsing exception", e);
        }
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager();
        FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, domainClass);
        if (sortFields != null && sortFields.length > 0)
        {
            Sort sort = new Sort(sortFields);
            fullTextQuery.setSort(sort);
        }

        return fullTextQuery;
    }

    @Override
    public int countBySearchQuery(SearchQuery query)
    {
        if ( isEmpty(query) )
        {
            return new Long(count()).intValue();
        }

        return createQuery(query.getQueryString(), null).getResultSize();
    }

    @Override
    public List<T> findAll(int currentPage, int maxResults)
    {
        javax.persistence.Query query = entityManager.createQuery("from PortalOffer order by AddedDate desc");
        query.setMaxResults(maxResults);
        query.setFirstResult(maxResults * currentPage);

        return query.getResultList();
    }

    @Override
    public List<T> findAllAndSort(int currentPage, int maxResults, String sort, boolean reverse)
    {
        javax.persistence.Query query = entityManager.createQuery("from " + domainClass.getSimpleName() + " order by " + sort + " " + getOrderBy(reverse));
        query.setMaxResults(maxResults);
        query.setFirstResult(maxResults * currentPage);

        return query.getResultList();
    }

    @Override
    public List<T> findAllBySearchQuery(SearchQuery searchQuery)
    {
        Assert.isTrue(!searchQuery.isEmpty());

        FullTextQuery query = createQuery(searchQuery.getQueryString(), getDefaultSortFields(searchQuery));
        List<T> list = query.getResultList();

        return list;
    }

    @Override
    public List<T> findAllBySearchQuery(SearchQuery searchQuery, int page, int maxQty)
    {
        if(searchQuery.isEmpty())
        {
            if(searchQuery.isSort())
            {
                SortField sortField = searchQuery.getSortFields().get(0);
                return findAllAndSort(page, maxQty, sortField.getField(), sortField.getReverse());
            }

            return findAll(page, maxQty);
        }

        FullTextQuery query = createQuery(searchQuery.getQueryString(), getDefaultSortFields(searchQuery));
        query.setMaxResults(maxQty);
        query.setFirstResult(maxQty * page);
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

    private String getOrderBy(boolean reverse)
    {
        return reverse ? DESC : ASC;
    }
}
