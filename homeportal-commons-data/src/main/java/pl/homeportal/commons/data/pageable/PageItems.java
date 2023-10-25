/**
 *
 */
package pl.homeportal.commons.data.pageable;

import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static pl.homeportal.commons.text.Constants.AMPERSAND;
import static pl.homeportal.commons.text.Constants.EQUALS_MARK;
import static pl.homeportal.commons.text.Constants.QUESTION_MARK;
import static pl.homeportal.commons.text.Constants.UTF_8;

@Getter
public class PageItems
{
    private static final int DEFAULT_PAGES_QTY = 3;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private List<PageItem> pageItems;

    public static PageItems of(Page form, int allResultsQty)
    {
        return of(form, allResultsQty, DEFAULT_PAGES_QTY, DEFAULT_PAGE_SIZE, emptySet());
    }

    private static PageItems of(Page form, int allResultsQty, int pagesQty, int pageSize)
    {
        return of(form, allResultsQty, pagesQty,pageSize, emptySet());
    }

    private static PageItems of(Page form, int allResultsQty, int pagesQty, int pageSize, Set<String> excludes)
    {
        int last = calculateLastPage(allResultsQty, pageSize);
        int current = form.getPageNumber();
        int start = calculateStart(current);
        int end = calculateEnd(start, pagesQty, last);

        PageItems pageItems = new PageItems();
        for (int i = start; i <= end; i++)
        {
            PageItem pageItem = new PageItem();
            if (i == current)
            {
                pageItem.current();
            }

            pageItem.setLabel(i);
            form.setPage(i);
            String link = pageableToUri(form, excludes);
            pageItem.setLink(link);
            pageItems.addPageItem(pageItem);
        }
        form.setPage(current);

        return pageItems;
    }

    public static String pageableToUri(Pageable form, Set<String> excludes)
    {
        try
        {
            StringBuilder sBuilder = new StringBuilder(QUESTION_MARK);
            List<Field> fields = getDeclaredFields(form);
            int index = 0;
            for (Field field : fields)
            {
                field.setAccessible(true);
                String fName = field.getName();
                Object value = field.get(form);

                if (!isFieldAcceptable(field, value, excludes))
                {
                    continue;
                }

                if (value instanceof String)
                {
                    if (isEmpty((String) value))
                    {
                        continue;
                    }
                    value = URLEncoder.encode((String) value, UTF_8);
                }

                if (index != 0)
                {
                    sBuilder.append(AMPERSAND);
                }
                sBuilder.append(fName);
                sBuilder.append(EQUALS_MARK);
                sBuilder.append(value.toString());
                ++index;
            }

            return sBuilder.toString();

        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static int calculateStart(int current)
    {
        int start = current - 1;
        return start > 0 ? start : 1;
    }

    private static int calculateEnd(int start, int pagesQty, int last)
    {
        int end = start + pagesQty - 1;
        return end > last ? last : end;
    }

    private void addPageItem(PageItem pageItem)
    {
        if (pageItems == null)
        {
            pageItems = new ArrayList<>();
        }
        this.pageItems.add(pageItem);
    }

    private static int calculateLastPage(int allResultsQty, int pageSize)
    {
        return (int) Math.ceil(allResultsQty / Double.valueOf(pageSize));
    }

    private static List<Field> getDeclaredFields(Pageable form)
    {
        List<Field> fields = new ArrayList<>();
        fields.addAll(asList(form.getClass().getDeclaredFields()));
        if (form.getClass().getSuperclass() != null)
        {
            fields.addAll(asList(form.getClass().getSuperclass().getDeclaredFields()));
        }
        return fields;
    }

    private static boolean isFieldAcceptable(Field field, Object value, Set<String> excludes)
    {
        if (Modifier.isStatic(field.getModifiers()))
        {
            return false;
        }

        if (excludes.contains(field.getName()))
        {
            return false;
        }

        if (value == null)
        {
            return false;
        }

        if ("LOG".equals(field.getName()))
        {
            return false;
        }

        return true;
    }
}
