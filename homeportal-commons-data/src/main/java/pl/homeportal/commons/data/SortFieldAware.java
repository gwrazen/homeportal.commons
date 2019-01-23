package pl.homeportal.commons.data;

import org.apache.lucene.search.SortField;

import java.util.List;

public interface SortFieldAware
{
    List<SortField> getSortFields();
}
