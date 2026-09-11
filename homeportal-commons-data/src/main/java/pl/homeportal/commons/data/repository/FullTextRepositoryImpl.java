package pl.homeportal.commons.data.repository;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.hibernate.CacheMode;
import org.hibernate.search.backend.lucene.LuceneExtension;
import org.hibernate.search.backend.lucene.index.LuceneIndexManager;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.entity.SearchIndexedEntity;
import org.hibernate.search.mapper.orm.mapping.SearchMapping;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.homeportal.commons.data.entity.AbstractEntity;
import pl.homeportal.commons.data.search.SearchQuery;
import pl.homeportal.commons.data.search.SortSpec;
import pl.homeportal.commons.exception.HomeportalServiceException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    private static final int BATCH_SIZE_TO_LOAD_OBJECTS = 100;
    private static final int THREADS_TO_LOAD_OBJECTS = 10;

    /**
     * ⚠️ Integer.MIN_VALUE to jedyna wartosc, przy ktorej sterownik MySQL-a strumieniuje wynik
     * zamiast wczytac go w calosci do pamieci. Bez tego przebudowa indeksu konczy sie
     * OutOfMemoryError w sterowniku, a nie w Lucene — i wyglada na za maly heap.
     */
    private static final int ID_FETCH_SIZE_STREAMING = Integer.MIN_VALUE;
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
        final S managed = t.isTransient() ? persist(t) : entityManager.merge(t);
        searchSession().indexingPlan().addOrUpdate(managed);

        return managed;
    }

    @Override
    public void indexedDelete(T t)
    {
        // Encja odlaczona (wczytana w innej transakcji) wymaga merge — samo remove
        // rzucalo dla niej IllegalArgumentException.
        final T managed = entityManager.contains(t) ? t : entityManager.merge(t);
        entityManager.remove(managed);
        searchSession().indexingPlan().purge(managed.getClass(), managed.getId(), null);
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
        searchSession().workspace(t).purge();
        searchSession().workspace(t).flush();
    }

    @Override
    public void purge(T t)
    {
        // purge i indexOne to para jawnych operacji na samym indeksie — obie publikuja od razu,
        // inaczej niz indexedSave/indexedDelete, ktore czekaja na commit. Bez flusha purge byl
        // jedyna z czworki, po ktorej nie dalo sie sprawdzic wyniku bez konczenia transakcji.
        final SearchSession session = searchSession();
        session.indexingPlan().purge(t.getClass(), t.getId(), null);
        session.indexingPlan().execute();
        session.workspace(t.getClass()).flush();
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
            // ⚠️ Do 5.x liczylo to zapytanie zakresowe "(id:[0 TO 999999999])" — sztuczka
            // na "wszystkie dokumenty", bo identyfikator byl zwyklym polem o nazwie "id".
            // W Search 6/7 identyfikator NIE jest polem indeksu pod ta nazwa, wiec ten zakres
            // nie trafia w nic i licznik oddawal zero przy niepustym indeksie. matchAll()
            // wyraza ten sam zamiar wprost.
            return searchSession().search(t).where(f -> f.matchAll()).fetchTotalHitCount();
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

        return (int) createQuery(sQuery.getQueryString(), null, sQuery.isKeywordAnalyser(), t).fetchTotalHitCount();
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

        jakarta.persistence.Query query = entityManager.createQuery(stringQuery);
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

        return createQuery(sQuery.getQueryString(), sQuery.getSortSpecs(), sQuery.isKeywordAnalyser(), t)
                .fetch(sQuery.getPageNumber() * sQuery.getPageSize(), sQuery.getPageSize())
                .hits();
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
            searchSession()
                    .massIndexer(t)
                    .batchSizeToLoadObjects(batchSize)
                    .threadsToLoadObjects(threads)
                    .idFetchSize(ID_FETCH_SIZE_STREAMING)
                    .cacheMode(CacheMode.IGNORE)
                    .mergeSegmentsOnFinish(true)
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
        final SearchSession session = searchSession();
        entity = entityManager.merge(entity);
        session.indexingPlan().addOrUpdate(entity);
        session.indexingPlan().execute();
        session.workspace(entity.getClass()).flush();
    }

    @Override
    public void optimizeIndex()
    {
        // optimize() z 5.x nazywa sie dzis mergeSegments() i siedzi na workspace, nie na fabryce.
        searchSession().workspace().mergeSegments();
    }

    /**
     * Szczegol implementacji — typy Lucene i Hibernate Search nie wychodza poza ta klase.
     *
     * Wolajacy podaja gotowy STRING zapytania Lucene'a, wiec zamiast DSL-a Search 6/7 idzie
     * natywne zapytanie przez LuceneExtension — dzieki temu skladnia zapytan po stronie portalu
     * i hopa zostaje nietknieta.
     *
     * ⚠️ {@code parser.setLowercaseExpandedTerms(true)} zniknelo w Lucene 7. Rozwijane termy
     * (wildcard, zakresy) nie sa juz obnizane do malych liter przez parser — robi to analizator
     * pola. Dla pol analizowanych zachowanie jest to samo, dla pol keyword ROZNI SIE i dlatego
     * zapytania z flaga keywordAnalyser musza byc sprawdzone osobno.
     */
    org.hibernate.search.engine.search.query.SearchQuery<T> createQuery(
            String queryString, List<SortSpec> sortSpecs, boolean keywordAnalyser, Class<T> t)
    {
        try
        {
            final QueryParser parser = new QueryParser(ID, getAnalyzer(keywordAnalyser, t));
            final Query luceneQuery = parser.parse(queryString);
            final org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep<?, T, ?, ?, ?> step =
                    searchSession().search(t)
                            .extension(LuceneExtension.get())
                            .where(f -> f.fromLuceneQuery(luceneQuery));

            if (sortSpecs != null && !sortSpecs.isEmpty())
            {
                // ⚠️ NIE natywny org.apache.lucene.search.Sort. Search 6/7 zapisuje pole sortowalne
                // jako docvalues SORTED_SET (bo dopuszcza wielowartosciowosc), a SortField.Type.STRING
                // z Lucene'a zada SORTED i wywala sie komunikatem "unexpected docvalues type".
                // DSL Search dobiera wlasciwy typ sam.
                return step.sort(f -> {
                    final var composite = f.composite();
                    for (SortSpec spec : sortSpecs)
                    {
                        composite.add(spec.isReverse()
                                              ? f.field(spec.getField()).desc()
                                              : f.field(spec.getField()).asc());
                    }
                    return composite;
                }).toQuery();
            }

            return step.toQuery();
        }
        catch (Exception e)
        {
            throw new HomeportalServiceException("Could not execute full-text query: " + queryString, e);
        }
    }

    private <S extends T> S persist(S t)
    {
        // merge dla encji transientnej zwracal kopie, a przekazany obiekt zostawal
        // bez identyfikatora — wolajacy ignorujacy wynik trzymal wiec obiekt bez id.
        entityManager.persist(t);
        return t;
    }

    private SearchSession searchSession()
    {
        return Search.session(entityManager);
    }

    private SearchMapping searchMapping()
    {
        return Search.mapping(entityManager.getEntityManagerFactory());
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

        final Class<?> indexedType = resolveIndexedType(t);
        if (indexedType == null)
        {
            return DEFAULT_ANALYZER;
        }

        // Analizator wyszukiwania siedzi dzis w menedzerze indeksu backendu Lucene'a —
        // SearchFactory#getAnalyzer(Class) z 5.x nie ma odpowiednika.
        return searchMapping().indexedEntity(indexedType)
                              .indexManager()
                              .unwrap(LuceneIndexManager.class)
                              .searchAnalyzer();
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
    private Class<?> resolveIndexedType(Class<T> t)
    {
        Class<?> resolved = null;
        for (SearchIndexedEntity<?> entity : searchMapping().allIndexedEntities())
        {
            final Class<?> candidate = entity.javaClass();
            if (candidate.equals(t))
            {
                return t;
            }
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
