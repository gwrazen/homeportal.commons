package pl.homeportal.commons.data.search;

import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.SortField;
import pl.homeportal.commons.data.SortFieldAware;
import pl.homeportal.commons.data.search.encoding.ValueEncoders;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Grzegorz Wrazen
 */
public class SearchQuery implements SortFieldAware
{
    /** Otwarty koniec zakresu — zastepuje sentinele w rodzaju "9999999" po stronie konsumenta. */
    public static final String UNBOUNDED = "*";

    private static final String AND = " AND ";
    private static final String OR = " OR ";

    private final List<String> parameters = new LinkedList<>();
    private final LinkedList<SortField> sortFields = new LinkedList<>();

    @Setter
    @Getter
    private int pageNumber = 0;

    @Setter
    @Getter
    private int pageSize = 20;

    @Setter
    @Getter
    private boolean keywordAnalyser;

    public void addParameter(QueryParameter parameter, String value)
    {
        final String term = term(parameter, value);
        if (term == null)
        {
            return;
        }

        parameters.add("(" + term + ")");
    }

    public void addNotParameter(QueryParameter parameter, String value)
    {
        final String term = term(parameter, value);
        if (term == null)
        {
            return;
        }

        parameters.add("(*:* AND NOT " + term + ")");
    }

    public void addOrParameter(QueryParameter parameter, String first, String second)
    {
        final String firstTerm = term(parameter, first);
        final String secondTerm = term(parameter, second);

        if (firstTerm == null || secondTerm == null)
        {
            // Klauzula OR z jedna nieznana wartoscia to zwykly term, a nie blad.
            final String single = firstTerm != null ? firstTerm : secondTerm;
            if (single != null)
            {
                parameters.add("(" + single + ")");
            }
            return;
        }

        parameters.add("(" + firstTerm + OR + secondTerm + ")");
    }

    /**
     * Fraza jest kodowana tak samo jak zwykly term — wczesniej byla jedynym
     * builderem, ktory pomijal normalizacje, wiec przeciw polu z usunietymi
     * spacjami nie mogla trafic nigdy.
     */
    public void addPhraseParameter(QueryParameter parameter, String value)
    {
        final String encoded = encode(parameter, value);
        if (encoded == null)
        {
            return;
        }

        parameters.add("(" + parameter.getValue() + ":\"" + escape(encoded) + "\")");
    }

    public void addRangeParameter(QueryParameter parameter, String from, String to)
    {
        parameters.add("(" + parameter.getValue() + ":[" + bound(parameter, from) + " TO " + bound(parameter, to) + "])");
    }

    /**
     * Zakres otwarty z gory: [x TO *]. Bez tego konsumenci musieli podawac sentinel
     * (hop uzywal "9999999", przez co oferty powyzej ~10 mln PLN wypadaly z wynikow).
     */
    public void addRangeFrom(QueryParameter parameter, String from)
    {
        addRangeParameter(parameter, from, null);
    }

    /** Zakres otwarty z dolu: [* TO x]. */
    public void addRangeTo(QueryParameter parameter, String to)
    {
        addRangeParameter(parameter, null, to);
    }

    public void addDateRangeParameter(QueryParameter parameter, Date from, Date to)
    {
        final String start = from == null ? UNBOUNDED : ValueEncoders.DATE.encode(from);
        final String end = to == null ? UNBOUNDED : ValueEncoders.DATE.encode(to);

        parameters.add("(" + parameter.getValue() + ":[" + start + " TO " + end + "])");
    }

    public int getParameterQty()
    {
        return parameters.size();
    }

    public boolean isQueryEmpty()
    {
        return parameters.isEmpty();
    }

    public boolean isSortEmpty()
    {
        return sortFields.isEmpty();
    }

    public String getQueryString()
    {
        return String.join(AND, parameters);
    }

    public LinkedList<SortField> getSortFields()
    {
        return sortFields;
    }

    public void addSortField(QueryParameter parameter)
    {
        if (parameter == null)
        {
            return;
        }
        sortFields.addLast(new SortField(parameter.getValue(), SortField.Type.STRING));
    }

    public void addSortField(QueryParameter parameter, boolean reverse)
    {
        if (parameter == null)
        {
            return;
        }
        sortFields.addLast(new SortField(parameter.getValue(), SortField.Type.STRING, reverse));
    }

    @Override
    public String toString()
    {
        return getQueryString();
    }

    private String term(QueryParameter parameter, String value)
    {
        final String encoded = encode(parameter, value);

        return encoded == null ? null : parameter.getValue() + ":" + escape(encoded);
    }

    /**
     * Kodowanie bierze sie z deklaracji parametru, a nie z zaszytego na sztywno
     * bridge'a — to jest cala istota poprawki: strona zapytania i strona indeksu
     * korzystaja z tej samej implementacji.
     */
    private String encode(QueryParameter parameter, String value)
    {
        if (parameter == null || value == null || value.trim().isEmpty())
        {
            return null;
        }

        return parameter.encoder().encode(value);
    }

    private String bound(QueryParameter parameter, String value)
    {
        if (value == null || value.trim().isEmpty() || UNBOUNDED.equals(value.trim()))
        {
            return UNBOUNDED;
        }

        final String encoded = ValueEncoders.NUMERIC.encode(value);

        // Wartosc nienumeryczna nie moze cicho zamienic sie w otwarty koniec zakresu.
        if (encoded == null)
        {
            throw new IllegalArgumentException("Range bound is not a number: '" + value + "' for parameter: "
                                               + parameter.getValue());
        }

        return encoded;
    }

    /**
     * Bez escapowania wartosc sterowana przez uzytkownika mogla przestawic pole
     * zapytania (term "x OR product:1" sklejal sie w "xORproduct:1", co parser
     * czytal jako klauzule na polu xORproduct) albo po prostu wywrocic parser.
     */
    private String escape(String value)
    {
        return QueryParser.escape(value);
    }
}
