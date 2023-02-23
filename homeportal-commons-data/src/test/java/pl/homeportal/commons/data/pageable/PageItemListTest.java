package pl.homeportal.commons.data.pageable;

import lombok.AllArgsConstructor;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class PageItemListTest
{

    private final int allResultsQty = 51;
    private final int pagesQty = 3;
    private final int pageSize = 10;

    /**
     *  set: [1],2,3
     */
    @Test
    public void testRangeOnePageOne()
    {
        // given
        PageItemList pageItemList = PageItemList.of(getPage(1), allResultsQty, pagesQty, pageSize, emptySet());

        // when
        List<PageItem> pages = pageItemList.getPageItems();

        // then
        assertThat(pages.size(), equalTo(3));
        assertThat(pages.get(0).isCurrent(), equalTo(true));
        assertThat(pages.get(1).isCurrent(), equalTo(false));
        assertThat(pages.get(2).isCurrent(), equalTo(false));

        assertThat(pages.get(0).getLabel(), equalTo(1));
        assertThat(pages.get(1).getLabel(), equalTo(2));
        assertThat(pages.get(2).getLabel(), equalTo(3));
    }

    /**
     * set: 1,[2],3
     */
    @Test
    public void testRangeOnePageTwo()
    {
        // given
        PageItemList pageItemList = PageItemList.of(getPage(2), allResultsQty, pagesQty, pageSize, emptySet());

        // when
        List<PageItem> pages = pageItemList.getPageItems();

        // then
        assertThat(pages.size(), equalTo(3));
        assertThat(pages.get(0).isCurrent(), equalTo(false));
        assertThat(pages.get(1).isCurrent(), equalTo(true));
        assertThat(pages.get(2).isCurrent(), equalTo(false));

        assertThat(pages.get(0).getLabel(), equalTo(1));
        assertThat(pages.get(1).getLabel(), equalTo(2));
        assertThat(pages.get(2).getLabel(), equalTo(3));
    }

    /**
     *  set: 2,[3],4
     */
    @Test
    public void testRangeOnePageThree()
    {
        // given
        PageItemList pageItemList = PageItemList.of(getPage(3), allResultsQty, pagesQty, pageSize, emptySet());

        // when
        List<PageItem> pages = pageItemList.getPageItems();

        // then
        assertThat(pages.size(), equalTo(3));
        assertThat(pages.get(0).isCurrent(), equalTo(false));
        assertThat(pages.get(1).isCurrent(), equalTo(true));
        assertThat(pages.get(2).isCurrent(), equalTo(false));

        assertThat(pages.get(0).getLabel(), equalTo(2));
        assertThat(pages.get(1).getLabel(), equalTo(3));
        assertThat(pages.get(2).getLabel(), equalTo(4));
    }

    /**
     *  set: 3,[4],5
     */
    @Test
    public void testRangeOnePageFour()
    {
        // given
        PageItemList pageItemList = PageItemList.of(getPage(4), allResultsQty, pagesQty, pageSize, emptySet());

        // when
        List<PageItem> pages = pageItemList.getPageItems();

        // then
        assertThat(pages.size(), equalTo(3));
        assertThat(pages.get(0).isCurrent(), equalTo(false));
        assertThat(pages.get(1).isCurrent(), equalTo(true));
        assertThat(pages.get(2).isCurrent(), equalTo(false));

        assertThat(pages.get(0).getLabel(), equalTo(3));
        assertThat(pages.get(1).getLabel(), equalTo(4));
        assertThat(pages.get(2).getLabel(), equalTo(5));
    }

    /**
     *  set: 4,[5],6
     */
    @Test
    public void testRangeOnePageFive()
    {
        // given
        PageItemList pageItemList = PageItemList.of(getPage(5), allResultsQty, pagesQty, pageSize, emptySet());

        // when
        List<PageItem> pages = pageItemList.getPageItems();

        // then
        assertThat(pages.size(), equalTo(3));
        assertThat(pages.get(0).isCurrent(), equalTo(false));
        assertThat(pages.get(1).isCurrent(), equalTo(true));
        assertThat(pages.get(2).isCurrent(), equalTo(false));

        assertThat(pages.get(0).getLabel(), equalTo(4));
        assertThat(pages.get(1).getLabel(), equalTo(5));
        assertThat(pages.get(2).getLabel(), equalTo(6));
    }

    /**
     *  set: 5,[6]
     */
    @Test
    public void testRangeOnePageSix()
    {
        // given
        PageItemList pageItemList = PageItemList.of(getPage(6), allResultsQty, pagesQty, pageSize, emptySet());

        // when
        List<PageItem> pages = pageItemList.getPageItems();

        // then
        assertThat(pages.size(), equalTo(2));
        assertThat(pages.get(0).isCurrent(), equalTo(false));
        assertThat(pages.get(1).isCurrent(), equalTo(true));

        assertThat(pages.get(0).getLabel(), equalTo(5));
        assertThat(pages.get(1).getLabel(), equalTo(6));
    }

    private InnerPage getPage(int index)
    {
        InnerPage page = InnerPage.of();
        page.setPage(index);
        return page;
    }

    @AllArgsConstructor(staticName = "of")
    static class InnerPage extends Page
    {
    }
}
