package pl.homeportal.commons.data.search;

/**
 * Created by Grzegorz Wrażeń on 25-04-2019
 */

public interface Pageable
{
    int getPageNumber();

    int getPageSize();

    String getSortField();

    boolean isReverseOrder();
}
