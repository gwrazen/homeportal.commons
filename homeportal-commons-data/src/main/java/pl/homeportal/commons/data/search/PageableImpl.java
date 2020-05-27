package pl.homeportal.commons.data.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Grzegorz Wrażeń on 27-05-2020
 */

@Setter
public class PageableImpl implements Pageable
{
    @Getter(AccessLevel.NONE)
    private int page = 1;

    @Getter(AccessLevel.NONE)
    private int maxqty = 20;

    @Getter(AccessLevel.NONE)
    private String sort = "added";

    @Getter(AccessLevel.NONE)
    private boolean reverse = true;

    @Override
    public int getPageNumber()
    {
        return page;
    }

    @Override
    public int getPageSize()
    {
        return maxqty;
    }

    @Override
    public String getSortField()
    {
        return sort;
    }

    @Override
    public boolean isReverseOrder()
    {
        return reverse;
    }

    @Override
    public void setPage(int page)
    {
        this.page = page;
    }
}
