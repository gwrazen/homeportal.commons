package pl.homeportal.commons.data.pageable;

import lombok.AllArgsConstructor;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * pagesQty: 3
 * _   _   _   _   _
 * 1,2,3,4,5,6,7,8,9
 * all first elements: 1,3,5,7,9
 *
 * [1],2,3
 * 1,[2],3
 * 2,[3],4
 * 3,[4],5
 * 4,[5],6
 *
 */
public class PageItemsTest
{
    private final int allResultsQty = 51;

    /**
     * set: [1],2,3
     */
    @Test
    public void testPageOne()
    {
        // given
        final int current = 1;
        PageItems pageItemList = PageItems.of(getPage(current), allResultsQty);

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
    public void testPageTwo()
    {
        // given
        final int current = 2;
        PageItems pageItemList = PageItems.of(getPage(current), allResultsQty);

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
     * set: 2,[3],4
     */
    @Test
    public void testPageThree()
    {
        // given
        PageItems pageItemList = PageItems.of(getPage(3), allResultsQty);

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
     * set: 3,[4],5
     */
    @Test
    public void testPageFour()
    {
        // given
        PageItems pageItemList = PageItems.of(getPage(4), allResultsQty);

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
     * set: 4,[5],6
     */
    @Test
    public void testPageFive()
    {
        // given
        final int current = 5;
        PageItems pageItemList = PageItems.of(getPage(current), allResultsQty);

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
     * set: 5,[6]
     */
    @Test
    public void testPageSix()
    {
        // given
        PageItems pageItemList = PageItems.of(getPage(6), allResultsQty);

        // when
        List<PageItem> pages = pageItemList.getPageItems();

        // then
        assertThat(pages.size(), equalTo(2));
        assertThat(pages.get(0).isCurrent(), equalTo(false));
        assertThat(pages.get(1).isCurrent(), equalTo(true));

        assertThat(pages.get(0).getLabel(), equalTo(5));
        assertThat(pages.get(1).getLabel(), equalTo(6));
    }

    /**
     * Regresja: przy zerowej liczbie wynikow petla budujaca elementy nie wykonywala
     * sie ani razu, wiec leniwie inicjalizowane pole zostawalo null — a widok
     * wywracal sie NPE na kazdej pustej liscie wynikow.
     */
    @Test
    public void testNoResults()
    {
        // given
        PageItems pageItems = PageItems.of(getPage(1), 0);

        // when
        List<PageItem> pages = pageItems.getPageItems();

        // then
        assertThat(pages, notNullValue());
        assertThat(pages.size(), equalTo(0));
    }

    /**
     * Regresja: budowanie linkow podmienialo numer strony w formularzu zwiazanym
     * z zadaniem i przywracalo go dopiero na koncu petli.
     */
    @Test
    public void testFormIsNotMutated()
    {
        // given
        final InnerPage form = getPage(2);

        // when
        PageItems.of(form, allResultsQty);

        // then
        assertThat(form.getPageNumber(), equalTo(2));
    }

    private InnerPage getPage(int index)
    {
        InnerPage page = InnerPage.of();
        page.setPage(index);
        page.setSize(10);
        return page;
    }

    @AllArgsConstructor(staticName = "of")
    static class InnerPage extends Page
    {
    }
}
