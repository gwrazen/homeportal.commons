/**
 *
 */
package pl.homeportal.commons.data.pageable;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static pl.homeportal.commons.text.Constants.AMPERSAND;
import static pl.homeportal.commons.text.Constants.EQUALS_MARK;
import static pl.homeportal.commons.text.Constants.QUESTION_MARK;
import static pl.homeportal.commons.text.Constants.UTF_8;

@Getter
@Setter
public class PageItemList
{
    private static final int RANGE = 10;
    private static final int LIMIT = RANGE / 2;

    private PageItem back;
    private PageItem next;
    private List<PageItem> pageItems;

    public void addPageItem(PageItem pageItem)
    {
        if (pageItems == null)
        {
            pageItems = new ArrayList<>();
        }
        this.pageItems.add(pageItem);
    }

    public static PageItemList of(Page form, int allQuantity, int size, Set<String> excludes)
    {
        int lastPage = getLastPageIndex(allQuantity, size);
        int currentPage = form.getPageNumber();
        int startPage = currentPage - LIMIT - 1;
        int endPage = currentPage + LIMIT - 1;

        if (startPage < 1)
        {
            startPage = 1;
        }

        if (endPage > lastPage)
        {
            endPage = lastPage;
        }

        if (endPage < 2)
        {
            endPage = 0;
        }

        PageItemList pageItemList = new PageItemList();
        for (int i = startPage; i <= endPage; i++)
        {
            PageItem pageItem = new PageItem();
            if (i == currentPage)
            {
                pageItem.setCurrent(TRUE.toString());
            }

            pageItem.setLabel(String.valueOf(i));
            form.setPage(i);
            String link = pageableToUri(form, excludes);
            pageItem.setLink(link);
            pageItemList.addPageItem(pageItem);
        }
        form.setPage(currentPage);

        return pageItemList;
    }

    private static int getLastPageIndex(int allQuantity, int size)
    {
        return (int) Math.ceil(allQuantity / Double.valueOf(size));
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
