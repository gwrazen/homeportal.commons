package pl.homeportal.commons.data.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Created by Grzegorz Wrazen on 27-05-2020
 */

@Setter
public class Page extends PageRequest
{
    private static final String DEFAULT_SORT = "added";

    @Getter(AccessLevel.NONE)
    private int page = 1;

    @Getter(AccessLevel.NONE)
    private int maxqty = 20;

    @Getter(AccessLevel.NONE)
    private String sort = DEFAULT_SORT;

    @Getter(AccessLevel.NONE)
    private boolean reverse = true;

    public Page()
    {
        super(0, 20, Sort.by(Sort.Order.desc(DEFAULT_SORT)));
    }

    public Page(int page, int size)
    {
        super(page, size);
    }

    public Page(int page, int size, Sort.Direction direction, String... properties)
    {
        super(page, size, direction, properties);
    }

    public Page(int page, int size, Sort sort)
    {
        super(page, size, sort);
    }

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

    public String getSortField()
    {
        return sort;
    }

    public boolean isReverseOrder()
    {
        return reverse;
    }
}
