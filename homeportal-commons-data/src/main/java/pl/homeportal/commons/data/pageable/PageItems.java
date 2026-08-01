/**
 *
 */
package pl.homeportal.commons.data.pageable;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static pl.homeportal.commons.text.Constants.AMPERSAND;
import static pl.homeportal.commons.text.Constants.EQUALS_MARK;
import static pl.homeportal.commons.text.Constants.QUESTION_MARK;
import static pl.homeportal.commons.text.Constants.UTF_8;

@Getter
public class PageItems
{
    private static final Logger LOG = LoggerFactory.getLogger(PageItems.class);

    private static final int DEFAULT_PAGES_QTY = 3;
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Inicjalizowana od razu: przy zerowej liczbie wynikow petla budujaca elementy
     * nie wykonywala sie ani razu, wiec getPageItems() zwracalo null — a widok
     * wywracal sie NPE na kazdej pustej liscie wynikow.
     */
    private final List<PageItem> pageItems = new ArrayList<>();

    public static PageItems of(Page form, int allResultsQty)
    {
        return of(form, allResultsQty, DEFAULT_PAGES_QTY, form.getPageSize(), emptySet());
    }

    public static PageItems of(Page form, int allResultsQty, Set<String> excludes, Map<String, String> defaults)
    {
        return of(form, allResultsQty, DEFAULT_PAGES_QTY, form.getPageSize(), excludes, defaults);
    }

    private static PageItems of(Page form, int allResultsQty, int pagesQty, int pageSize)
    {
        return of(form, allResultsQty, pagesQty, pageSize, emptySet());
    }

    private static PageItems of(Page form, int allResultsQty, int pagesQty, int pageSize, Set<String> excludes)
    {
        return of(form, allResultsQty, pagesQty, pageSize, excludes, emptyMap());
    }

    private static PageItems of(Page form, int allResultsQty, int pagesQty, int pageSize, Set<String> excludes, Map<String, String> defaults)
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
            // Kopia zamiast mutowania formularza zwiazanego z zadaniem: wyjatek
            // w srodku petli zostawial go z podmieniona strona, ktora plynela dalej
            // do modelu i widoku.
            String link = pageableToUri(pageForLink(form, i), excludes, defaults);
            if (isBlank(link))
            {
                pageItem.setLink(".");
            }
            else
            {
                pageItem.setLink(link);
            }
            pageItems.addPageItem(pageItem);
        }
        return pageItems;
    }

    private static Page pageForLink(Page form, int pageNumber)
    {
        final Page copy = new Page(pageNumber, form.getPageSize(), form.getSort());
        return copy;
    }

    public static String pageableToUri(Pageable form, Set<String> excludes)
    {
        return pageableToUri(form, excludes, emptyMap());
    }

    public static String pageableToUri(Pageable form, Set<String> excludes, Map<String, String> defaults)
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

                if (!isFieldAcceptable(field, value, excludes, defaults))
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

            final String uri = sBuilder.toString();
            if (uri.endsWith(QUESTION_MARK))
            {
                return uri.substring(0, uri.length() - QUESTION_MARK.length());
            }
            return sBuilder.toString();

        }
        catch (Exception e)
        {
            LOG.warn("Could not build a pager link from: {}", form, e);
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

    private static boolean isFieldAcceptable(Field field, Object value, Set<String> excludes, Map<String, String> defaults)
    {
        if (Modifier.isStatic(field.getModifiers()))
        {
            return false;
        }

        if (excludes.contains(field.getName()))
        {
            return false;
        }

        // Kolejnosc ma znaczenie: wczesniej value.toString() bylo wolane PRZED
        // sprawdzeniem null, wiec puste pole formularza konczylo sie NPE — polkniete
        // przez blanket catch i zamienione na link ".".
        if (value == null)
        {
            return false;
        }

        if (defaults.containsKey(field.getName()) && defaults.get(field.getName()).equals(value.toString()))
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
