package pl.homeportal.commons.data.pageable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Created by Grzegorz Wrazen on 27-05-2020
 */

public class Page extends PageRequest
{
    private static final String DEFAULT_SORT = "added";
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;

    @Setter
    private int page = DEFAULT_PAGE; 

    @Setter
    private int size = DEFAULT_SIZE;

    @Getter(AccessLevel.NONE)
    private String sort = DEFAULT_SORT;

    @Getter(AccessLevel.NONE)
    private boolean reverse = true;

    public Page()
    {
        super(DEFAULT_PAGE, DEFAULT_SIZE, Sort.by(Sort.Order.desc(DEFAULT_SORT)));
    }

    public Page(int page, int size)
    {
        super(page, size, Sort.unsorted());
        this.page = page;
        this.size = size;
    }

//    public Page(int page, int size, Sort.Direction direction, String... properties)
//    {
//        super(page, size, direction, properties);
//        this.page = page;
//        this.size = size;
//    }

    public Page(int page, int size, Sort sort)
    {
        super(page, size, sort);
        this.page = page;
        this.size = size;
        this.sort = sort.toString();
    }

    public String getSortField()
    {
        return sort;
    }

    public boolean isReverseOrder()
    {
        return reverse;
    }

    @Override
    public int getPageNumber()
    {
        return page;
    }

    @Override
    public int getPageSize()
    {
        return size;
    }
}
