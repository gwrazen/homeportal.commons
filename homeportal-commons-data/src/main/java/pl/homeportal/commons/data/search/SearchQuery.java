package pl.homeportal.commons.data.search;

import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.search.SortField;
import pl.homeportal.commons.data.SortFieldAware;
import pl.homeportal.commons.data.search.bridge.NumericBridge;
import pl.homeportal.commons.data.search.bridge.PropertyTypeBridge;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Grzegorz Wrazen
 */
public class SearchQuery implements SortFieldAware
{
    private final List<String> parameters = new LinkedList<>();
    private final LinkedList<SortField> sortFields = new LinkedList<>();
    private final PropertyTypeBridge propertyTypeBridge = new PropertyTypeBridge();

    @Setter
    @Getter
    private int pageNumber = 0;

    @Setter
    @Getter
    private int pageSize = 20;

    public void addParameter(QueryParameter parameter, String value)
    {
        StringBuilder query = new StringBuilder();
        query.append("(" + parameter.getValue() + ":" + normalize(value) + ")");

        parameters.add(query.toString());
    }

    public void addNotParameter(QueryParameter parameter, String value)
    {
        final StringBuilder query = new StringBuilder();
        query.append("(*:* AND NOT " + parameter.getValue() + ":" + normalize(value) + ")");

        parameters.add(query.toString());
    }

    public void addOrParameter(QueryParameter type, String first, String second)
    {
        StringBuilder query = new StringBuilder();

        query.append("(" + type.getValue() + ":" + normalize(first));
        query.append(" OR ");
        query.append(type.getValue() + ":" + normalize(second) + ")");

        parameters.add(query.toString());
    }

    public void addPhraseParameter(QueryParameter parameter, String value)
    {
        StringBuilder query = new StringBuilder();
        query.append("(" + parameter.getValue() + ":\"" + value + "\")");

        parameters.add(query.toString());
    }


    public void addRangeParameter(QueryParameter type, String from, String to)
    {
        StringBuilder query = new StringBuilder();

        query.append("(" + type.getValue() + ":[" + longToString(Long.valueOf(from)) + " TO " + longToString(Long.valueOf(to)) + "])");
        parameters.add(query.toString());
    }

    public void addDateRangeParameter(QueryParameter type, Date from, Date to)
    {
        StringBuilder query = new StringBuilder();
        query.append(type.getValue());
        query.append(":[");
        query.append(DateTools.dateToString(from, Resolution.SECOND));
        query.append(" TO ");
        query.append(DateTools.dateToString(to, Resolution.SECOND));
        query.append("]");

        parameters.add(query.toString());
    }


    public int getParameterQty()
    {
        return parameters.size();
    }

    public boolean isQueryEmpty()
    {
        return parameters.isEmpty() ? true : false;
    }

    public boolean isSortEmpty()
    {
        return sortFields.isEmpty() ? false : true;
    }

    public String getQueryString()
    {
        StringBuilder query = new StringBuilder();
        for (int index = 0; index < parameters.size(); ++index)
        {
            String parameter = parameters.get(index);
            if (index == 0)
            {
                query.append(parameter);

            }
            else
            {
                query.append(" AND ");
                query.append(parameter);
            }

        }
        return query.toString();
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

    private String longToString(long l)
    {
        NumericBridge numericBridge = new NumericBridge();
        return numericBridge.objectToString(new Long(l));
    }

    private Object normalize(String string)
    {
        return propertyTypeBridge.objectToString(string);
    }

    @Override
    public String toString()
    {
        return getQueryString();
    }
}
