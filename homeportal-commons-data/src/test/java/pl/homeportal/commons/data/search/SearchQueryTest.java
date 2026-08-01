package pl.homeportal.commons.data.search;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.junit.Test;
import pl.homeportal.commons.data.search.encoding.ValueEncoder;
import pl.homeportal.commons.data.search.encoding.ValueEncoders;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SearchQueryTest
{
    private enum TestParameter implements QueryParameter
    {
        CITY("city"),
        PRICE("price")
                {
                    @Override
                    public ValueEncoder encoder()
                    {
                        return ValueEncoders.NUMERIC;
                    }
                },
        FEATURES("features")
                {
                    @Override
                    public ValueEncoder encoder()
                    {
                        return ValueEncoders.FEATURE;
                    }
                },
        ADDED("added");

        private final String value;

        TestParameter(String value)
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
            for (TestParameter parameter : values())
            {
                if (parameter.value.equals(value))
                {
                    return parameter;
                }
            }
            return null;
        }
    }

    @Test
    public void parameterUsesTheEncoderDeclaredByTheParameter()
    {
        final SearchQuery query = new SearchQuery();
        query.addParameter(TestParameter.CITY, "Nowy Sącz");
        query.addParameter(TestParameter.FEATURES, "wtórny");

        // miasto: bez spacji, bez ogonkow; cecha: z ogonkami
        assertEquals("(city:nowysacz) AND (features:wtórny)", query.getQueryString());
    }

    /**
     * Regresja: term "x OR product:1" sklejal sie w "xORproduct:1", co parser czytal
     * jako klauzule na polu sterowanym przez uzytkownika.
     */
    @Test
    public void escapesLuceneSyntaxInUserValues() throws Exception
    {
        final SearchQuery query = new SearchQuery();
        query.addParameter(TestParameter.CITY, "x OR product:1");

        assertTrue(query.getQueryString().contains("\\:"));
        // zapytanie musi byc parsowalne i nie moze wprowadzac drugiego pola
        new QueryParser("id", new KeywordAnalyzer()).parse(query.getQueryString());
    }

    @Test
    public void queryWithSpecialCharactersStaysParseable() throws Exception
    {
        final SearchQuery query = new SearchQuery();
        query.addParameter(TestParameter.CITY, "a[b]c~d^e{f}");

        new QueryParser("id", new KeywordAnalyzer()).parse(query.getQueryString());
    }

    @Test
    public void openEndedRangeReplacesSentinelBound()
    {
        final SearchQuery query = new SearchQuery();
        query.addRangeFrom(TestParameter.PRICE, "500000");

        assertTrue(query.getQueryString().endsWith("TO *])"));
    }

    @Test
    public void rangeUsesNumericEncodingOnBothBounds()
    {
        final SearchQuery query = new SearchQuery();
        query.addRangeParameter(TestParameter.PRICE, "100", "200");

        final String expected = "(price:[" + ValueEncoders.NUMERIC.encode("100")
                                + " TO " + ValueEncoders.NUMERIC.encode("200") + "])";
        assertEquals(expected, query.getQueryString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeRejectsNonNumericBound()
    {
        new SearchQuery().addRangeParameter(TestParameter.PRICE, "sto", "200");
    }

    @Test
    public void blankValuesAreSkippedInsteadOfProducingNullTerms()
    {
        final SearchQuery query = new SearchQuery();
        query.addParameter(TestParameter.CITY, null);
        query.addParameter(TestParameter.CITY, "   ");

        assertTrue(query.isQueryEmpty());
        assertEquals("", query.getQueryString());
    }

    @Test
    public void dateRangeAcceptsOpenEnd()
    {
        final SearchQuery query = new SearchQuery();
        query.addDateRangeParameter(TestParameter.ADDED, new Date(0), null);

        assertTrue(query.getQueryString().contains(" TO *]"));
    }

    /**
     * Regresja: isSortEmpty() zwracalo odwrotnosc swojej nazwy.
     */
    @Test
    public void isSortEmptyReportsTheTruth()
    {
        final SearchQuery query = new SearchQuery();
        assertTrue(query.isSortEmpty());

        query.addSortField(TestParameter.PRICE, true);
        assertFalse(query.isSortEmpty());
    }

    @Test
    public void parametersAreJoinedWithAnd()
    {
        final SearchQuery query = new SearchQuery();
        query.addParameter(TestParameter.CITY, "krakow");
        query.addNotParameter(TestParameter.CITY, "warszawa");

        assertEquals("(city:krakow) AND (*:* AND NOT city:warszawa)", query.getQueryString());
    }

    @Test
    public void phraseParameterIsEncodedLikeAnyOtherTerm()
    {
        final SearchQuery query = new SearchQuery();
        query.addPhraseParameter(TestParameter.FEATURES, "dom wolnostojący");

        assertEquals("(features:\"dom wolnostojący\")", query.getQueryString());
    }
}
