package pl.homeportal.commons.data.search;

import java.util.Objects;

/**
 * Deklaracja sortowania niezalezna od Lucene.
 *
 * Wczesniej ta role pelnil {@code org.apache.lucene.search.SortField}, wystawiony
 * wprost w publicznym API repozytorium — przez co kazde repozytorium konsumenta
 * kompilowalo sie przeciw Lucene 5 i blokowalo migracje na Hibernate Search 6.
 */
public final class SortSpec
{
    private final String field;
    private final boolean reverse;

    private SortSpec(String field, boolean reverse)
    {
        this.field = field;
        this.reverse = reverse;
    }

    public static SortSpec of(String field)
    {
        return new SortSpec(field, false);
    }

    public static SortSpec of(String field, boolean reverse)
    {
        return new SortSpec(field, reverse);
    }

    public String getField()
    {
        return field;
    }

    public boolean isReverse()
    {
        return reverse;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof SortSpec))
        {
            return false;
        }

        final SortSpec that = (SortSpec) other;

        return reverse == that.reverse && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(field, reverse);
    }

    @Override
    public String toString()
    {
        return field + (reverse ? " DESC" : " ASC");
    }
}
