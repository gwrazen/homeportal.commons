package pl.homeportal.commons.data.search;

/**
 * @author Grzegorz Wrażeń 2019-04-23
 */
public abstract class SearchQueryBuilder<SR extends Pageable>
{
    protected abstract SearchQuery build(SR sRequest);

    protected abstract void addSortField(SR sRequest, SearchQuery sQuery);

    protected void setPageable(SR sRequest, SearchQuery sQuery)
    {
        sQuery.setPageNumber(sRequest.getPageNumber() - 1);
        sQuery.setPageSize(sRequest.getPageSize());
    }
}