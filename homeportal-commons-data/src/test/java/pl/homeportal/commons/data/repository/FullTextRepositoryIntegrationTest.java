package pl.homeportal.commons.data.repository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.homeportal.commons.data.search.QueryParameter;
import pl.homeportal.commons.data.search.SearchQuery;
import pl.homeportal.commons.data.search.encoding.ValueEncoder;
import pl.homeportal.commons.data.search.encoding.ValueEncoders;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test end-to-end na bazie w pamieci (H2) i indeksie Lucene w pamieci.
 *
 * Jest jedynym miejscem, ktore dowodzi, ze kodowanie po stronie indeksu i po stronie
 * zapytania faktycznie sie spotykaja — testy jednostkowe pilnuja tylko, ze obie
 * strony wolaja ten sam encoder.
 */
public class FullTextRepositoryIntegrationTest
{
    private enum Parameter implements QueryParameter
    {
        CITY("city"),
        FEATURES("features")
                {
                    @Override
                    public ValueEncoder encoder()
                    {
                        return ValueEncoders.FEATURE;
                    }
                },
        PRICE("price")
                {
                    @Override
                    public ValueEncoder encoder()
                    {
                        return ValueEncoders.NUMERIC;
                    }
                };

        private final String value;

        Parameter(String value)
        {
            this.value = value;
        }

        @Override
        public String getValue()
        {
            return value;
        }

        @Override
        public QueryParameter getByValue(String value)
        {
            for (Parameter parameter : values())
            {
                if (parameter.value.equals(value))
                {
                    return parameter;
                }
            }
            return null;
        }
    }

    private EntityManagerFactory factory;
    private EntityManager entityManager;
    private FullTextRepositoryImpl<IndexedThing> repository;

    @Before
    public void setUp() throws Exception
    {
        factory = Persistence.createEntityManagerFactory("commons-it");
        entityManager = factory.createEntityManager();
        repository = new FullTextRepositoryImpl<>();

        final Field field = FullTextRepositoryImpl.class.getDeclaredField("entityManager");
        field.setAccessible(true);
        field.set(repository, entityManager);

        entityManager.getTransaction().begin();
        repository.indexedSave(new IndexedThing("Nowy Sącz", "|MARKET:wtórny||MEDIA:prąd|", 3_000_000_000L));
        repository.indexedSave(new IndexedThing("Kraków", "|MARKET:pierwotny|", 500_000L));
        repository.indexedSave(new IndexedThing("Nowy Sącz", "|MARKET:pierwotny|", 250_000L));
        entityManager.getTransaction().commit();
    }

    @After
    public void tearDown()
    {
        if (entityManager != null && entityManager.isOpen())
        {
            entityManager.close();
        }
        if (factory != null && factory.isOpen())
        {
            factory.close();
        }
    }

    /**
     * Regresja: nazwa dwuczlonowa byla indeksowana jako "nowysacz", ale zapytanie
     * dzialalo na polu analizowanym tokenami — filtr nie zwracal nic.
     */
    @Test
    public void findsByMultiWordCity()
    {
        final SearchQuery query = query();
        query.addParameter(Parameter.CITY, "Nowy Sącz");

        assertEquals(2, repository.findAllBySearchQuery(query, IndexedThing.class).size());
        assertEquals(2, repository.countBySearchQuery(query, IndexedThing.class));
    }

    /**
     * Regresja: FeatureBridge zapisywal "wtórny", a zapytanie szukalo "wtorny".
     */
    @Test
    public void findsByFeatureWithDiacritics()
    {
        final SearchQuery query = query();
        query.addParameter(Parameter.FEATURES, "wtórny");

        final List<IndexedThing> found = repository.findAllBySearchQuery(query, IndexedThing.class);

        assertEquals(1, found.size());
        assertEquals("Nowy Sącz", found.get(0).getCity());
    }

    /**
     * Regresja: intValue() zawijalo 3 mld na wartosc ujemna, wiec oferta wypadala
     * z kazdego zakresu cenowego.
     */
    @Test
    public void findsByRangeAboveIntegerMaxValue()
    {
        final SearchQuery query = query();
        query.addRangeFrom(Parameter.PRICE, "1000000000");

        final List<IndexedThing> found = repository.findAllBySearchQuery(query, IndexedThing.class);

        assertEquals(1, found.size());
        assertEquals(Long.valueOf(3_000_000_000L), found.get(0).getPrice());
    }

    @Test
    public void findsByClosedRange()
    {
        final SearchQuery query = query();
        query.addRangeParameter(Parameter.PRICE, "200000", "600000");

        assertEquals(2, repository.findAllBySearchQuery(query, IndexedThing.class).size());
    }

    @Test
    public void paginatesResults()
    {
        final SearchQuery query = query();
        query.addParameter(Parameter.CITY, "Nowy Sącz");
        query.setPageSize(1);

        query.setPageNumber(0);
        final List<IndexedThing> first = repository.findAllBySearchQuery(query, IndexedThing.class);
        query.setPageNumber(1);
        final List<IndexedThing> second = repository.findAllBySearchQuery(query, IndexedThing.class);

        assertEquals(1, first.size());
        assertEquals(1, second.size());
        assertTrue("Kolejne strony musza zwracac rozne wyniki",
                   !first.get(0).getId().equals(second.get(0).getId()));
    }

    @Test
    public void emptyQueryFallsBackToDatabaseListing()
    {
        assertEquals(3, repository.findAllBySearchQuery(query(), IndexedThing.class).size());
    }

    @Test
    public void countsIndexedDocuments()
    {
        assertEquals(3, repository.countByIndex(IndexedThing.class));
    }

    /**
     * Regresja: save() robilo merge takze dla encji transientnej, wiec przekazany
     * obiekt zostawal bez identyfikatora.
     */
    @Test
    public void indexedSaveAssignsIdentifierToThePassedInstance()
    {
        final IndexedThing thing = new IndexedThing("Gdańsk", "|MARKET:wtórny|", 700_000L);

        entityManager.getTransaction().begin();
        repository.indexedSave(thing);
        entityManager.getTransaction().commit();

        assertTrue("Przekazany obiekt musi dostac identyfikator", thing.isPersisted());
    }

    private SearchQuery query()
    {
        final SearchQuery query = new SearchQuery();
        query.setKeywordAnalyser(true);
        query.setPageSize(20);

        return query;
    }
}
