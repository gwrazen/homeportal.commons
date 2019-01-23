package pl.homeportal.commons.data.search;

public interface QueryParameter
{
    String getValue();

    QueryParameter getByValue(String value);
}