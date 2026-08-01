package pl.homeportal.commons.data.repository;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.hibernate.CacheMode;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.homeportal.commons.data.SortFieldAware;
import pl.homeportal.commons.data.entity.AbstractEntity;
import pl.homeportal.commons.data.search.SearchQuery;
import pl.homeportal.commons.data.search.SortSpec;
import pl.homeportal.commons.exception.HomeportalServiceException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static pl.homeportal.commons.text.Constants.SPACE;


@Transactional
public class FullTextRepositoryImpl<T extends AbstractEntity> implements FullTextRepository<T>
{
    private static final Logger LOG = LoggerFactory.getLogger(FullTextRepositoryImpl.class);

    public static final String SEARCH_QUERY_CANNOT_BE_NULL = "SearchQuery cannot be null!";
    public static final String DOCUMENTS_COUNT = "(id:[0 TO 999999999])";

    private static final int BATCH_SIZE_TO_LOAD_OBJECTS = 100;
    private static final int THREADS_TO_LOAD_OBJECTS = 10;
    private static final String ID = "id";

    /** Bezstanowy i wspoldzielony — inaczej niz poprzednia alokacja na kazde zapytanie. */
    private static final Analyzer KEYWORD_ANALYZER = new KeywordAnalyzer();

    /** Awaryjny analizator dla korzenia bez ani jednego zaindeksowanego podtypu — zachowanie 5.0. */
    private static final Analyzer DEFAULT_ANALYZER = new StandardAnalyzer();

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <S extends T> S indexedSave(S t)
    {
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager();
        final S managed = t.isTransient() ? persist(fullTextEntityManager, t) : fullTextEntityManager.merge(t);
        fullTextEntityManager.index(managed);

        return managed;
    }

    @Override
    public void indexedDelete(T t)
    {
        FullTextEntityManager fullTextEntityManager = getFullTextEntityManager();
        // Encja odlaczona (wczytana w innej transakcji) wymaga merge — samo remove
        // rzucalo dla niej IllegalArgumentException.
        final T managed = fullTextEntityManager.contains(t) ? t : fullTextEntityManager.merge(t);
        fullTextEntityManager.remove(managed);
        fullTextEntityManager.purge(managed.getClass(), managed.getId());
    }

    @Override
    public void deleteAll(Class<T> t)
    {
        // Bulk delete zamiast ladowania calej tabeli do pamieci i usuwania wiersz
        // po wierszu — poprzednia wersja konczyla sie OutOfMemoryError na duzych tabelach.
        entityManager.createQuery("delete from " + t.getSimpleName()).executeUpdate();

        // Bulk delete omija listenery Hibernate Search, wiec dokumenty musi usunac purgeAll —
        // i musi to zrobic od razu, bo po deleteAll indeks ma byc pusty niezaleznie od tego,
        // czy transakcja wolajacego kiedykolwiek sie zatwierdzi.
        final FullTextEntityManager fullTextEntityManager = getFullTextEntityManager();
        fullTextEntityManager.purgeAll(t);
        fullTextEntityManager.flushToIndexes();
    }

    @Override
    public void purge(T t)
    {
        // purge i indexOne to para jawnych operacji na samym indeksie — obie publikuja od razu,
        // inaczej niz indexedSave/indexedDelete, ktore czekaja na commit. Bez flusha purge byl
        // jedyna z czworki, po ktorej nie dalo sie sprawdzic wyniku bez konczenia transakcji.
        final FullTextEntityManager fullTextEntityManager = getFullTextEntityManager();
        fullTextEntityManager.purge(t.getClass(), t.getId());
        fullTextEntityManager.flushToIndexes();
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
            return createQuery(DOCUMENTS_COUNT, null, false, t).getResultSize();
        }
        catch (Exception e)
        {
            // Sentinel -1 nie byl sprawdzany przez zadnego wolajacego — trafial
            // wprost do komunikatu JMX jako liczba dokumentow.
            throw new HomeportalServiceException("Could not count indexed documents for: " + t.getSimpleName(), e);
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

        return createQuery(sQuery.getQueryString(), null, sQuery.isKeywordAnalyser(), t).getResultSize();
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

        FullTextQuery query = createQuery(sQuery.getQueryString(), toLuceneSortFields(sQuery), sQuery.isKeywordAnalyser(), t);
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
            // Bez przywrocenia flagi sygnal zamkniecia kontekstu ginal.
            Thread.currentThread().interrupt();
            throw new HomeportalServiceException("Indexing interrupted", e);
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

    private SortField[] toLuceneSortFields(SortFieldAware query)
    {
        final List<SortSpec> specs = query.getSortSpecs();
        final SortField[] sortFields = new SortField[specs.size()];
        for (int index = 0; index < specs.size(); index++)
        {
            final SortSpec spec = specs.get(index);
            sortFields[index] = new SortField(spec.getField(), SortField.Type.STRING, spec.isReverse());
        }

        return sortFields;
    }

    /** Szczegol implementacji — typy Lucene i Hibernate Search nie wychodza poza ta klase. */
    FullTextQuery createQuery(String queryString, SortField[] sortFields, boolean keywordAnalyser, Class<T> t)
    {
        try
        {
            QueryParser parser = new QueryParser(ID, getAnalyzer(keywordAnalyser, t));
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
            throw new HomeportalServiceException("Could not execute full-text query: " + queryString, e);
        }
    }

    private <S extends T> S persist(FullTextEntityManager fullTextEntityManager, S t)
    {
        // merge dla encji transientnej zwracal kopie, a przekazany obiekt zostawal
        // bez identyfikatora — wolajacy ignorujacy wynik trzymal wiec obiekt bez id.
        fullTextEntityManager.persist(t);
        return t;
    }

    private FullTextEntityManager getFullTextEntityManager()
    {
        return Search.getFullTextEntityManager(entityManager);
    }

    /**
     * Analizator pochodzi z fabryki Hibernate Search, czyli jest dokladnie tym,
     * ktorym pola encji byly analizowane przy indeksowaniu.
     *
     * Wczesniej budowany byl PerFieldAnalyzerWrapper z **pusta** mapa per-field,
     * co degenerowalo sie do jednego analizatora dla wszystkich pol wszystkich
     * encji (i dodatkowo alokowalo nowy, nigdy niezamykany Analyzer na kazde
     * zapytanie). Flaga keywordAnalyser wymusza analizator dokladnego dopasowania
     * dla zapytan, ktore maja trafiac w cala wartosc pola.
     */
    private Analyzer getAnalyzer(boolean keywordAnalyzer, Class<T> t)
    {
        if (keywordAnalyzer)
        {
            return KEYWORD_ANALYZER;
        }

        final SearchFactory searchFactory = getFullTextEntityManager().getSearchFactory();
        final Class<?> indexedType = resolveIndexedType(searchFactory, t);

        return indexedType == null ? DEFAULT_ANALYZER : searchFactory.getAnalyzer(indexedType);
    }

    /**
     * Korzeniem zapytania moze byc nadklasa, ktora sama nie jest {@code @Indexed} — Hibernate Search
     * celuje wtedy we wszystkie zaindeksowane podtypy. Tak odpytuje hop: {@code PortalOffer} jest
     * abstrakcyjny, a {@code @Indexed(index = "offers")} maja jego podklasy.
     *
     * {@code SearchFactory#getAnalyzer(Class)} jest zdefiniowane wylacznie dla typu zaindeksowanego
     * i dla takiego korzenia rzuca HSEARCH000109 z wnetrza fabryki. Do 5.0 problem nie wychodzil,
     * bo analizator byl budowany na sztywno i fabryki nie pytal wcale.
     *
     * Podtypy jednego korzenia dziela indeks, a wraz z nim analizator, wiec wybor pierwszego
     * z nich jest rownowazny wyborowi dowolnego. Kolejnosc jest ustalona po nazwie klasy, zeby
     * ten sam korzen zawsze dawal ten sam analizator.
     */
    private Class<?> resolveIndexedType(SearchFactory searchFactory, Class<T> t)
    {
        final Set<Class<?>> indexedTypes = searchFactory.getIndexedTypes();
        if (indexedTypes.contains(t))
        {
            return t;
        }

        Class<?> resolved = null;
        for (Class<?> candidate : indexedTypes)
        {
            if (t.isAssignableFrom(candidate)
                && (resolved == null || candidate.getName().compareTo(resolved.getName()) < 0))
            {
                resolved = candidate;
            }
        }

        return resolved;
    }

    private Pageable createPageable(SearchQuery sQuery)
    {
        final List<Sort.Order> orders = new ArrayList<>();
        for (SortSpec spec : sQuery.getSortSpecs())
        {
            // Brak else sprawial, ze pole z reverse emitowalo DESC, a zaraz po nim ASC
            // — czyli "order by X DESC, X ASC" dla kazdego odwroconego sortowania.
            orders.add(spec.isReverse() ? Sort.Order.desc(spec.getField()) : Sort.Order.asc(spec.getField()));
        }

        return PageRequest.of(sQuery.getPageNumber(), sQuery.getPageSize(),
                              orders.isEmpty() ? Sort.unsorted() : Sort.by(orders));
    }

    /**
     * Wczesniej metoda zwracala **pierwszy** order i konczyla petle, wiec sortowanie
     * po wiecej niz jednym polu bylo po cichu obcinane.
     */
    private String getSort(Sort sort)
    {
        final StringBuilder clause = new StringBuilder();
        for (Sort.Order order : sort)
        {
            if (clause.length() > 0)
            {
                clause.append(", ");
            }
            clause.append(order.getProperty()).append(SPACE).append(order.getDirection().name());
        }

        return clause.length() == 0 ? "id asc" : clause.toString();
    }
}
