package pl.homeportal.commons.data.search;

/**
 * @author Created by Grzegorz Wrażeń on 2019-04-25
 */
public interface PageInfo
{
    int getPageNumber();

    int getPageSize();

    String getSort();

    boolean isReverse();
}
