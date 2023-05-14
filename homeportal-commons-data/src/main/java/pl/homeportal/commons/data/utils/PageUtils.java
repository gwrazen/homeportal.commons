package pl.homeportal.commons.data.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * @author Created by Grzegorz Wrażeń on 2019-04-25
 */
public class PageUtils
{
    public static Pageable createPageable(int pageNumber, int pageSize, String sort, boolean reverse)
    {
        Sort.Direction direction = Sort.Direction.ASC;
        if (reverse)
        {
            direction = Sort.Direction.DESC;
        }

        return PageRequest.of(pageNumber, pageSize, direction, sort);
    }
}
