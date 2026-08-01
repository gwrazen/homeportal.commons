package pl.homeportal.commons.data.pageable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Objects;

/**
 * Formularz stronicowania wiazany z zadania HTTP.
 *
 * **Numeracja stron jest 1-based** (pierwsza strona to 1) — inaczej niz w Spring Data.
 * Nazwy pol (`page`, `size`, `sort`, `reverse`) sa jednoczesnie nazwami parametrow
 * zapytania w publicznym API hop-a, wiec nie moga sie zmienic.
 *
 * Zmiana w 6.0: klasa **implementuje** {@link Pageable} zamiast dziedziczyc po
 * {@link PageRequest}. Poprzednio przeslaniala pola nadklasy, ktora byla na stale
 * skonstruowana jako {@code PageRequest(1, 20, ...)} — przez co odziedziczone
 * {@code equals}/{@code hashCode}/{@code getOffset}/{@code next()} operowaly na
 * stanie niemajacym nic wspolnego z formularzem. Dwa formularze rozniace sie
 * numerem strony byly wobec siebie **rowne**, a {@code getOffset()} zwracalo stale 20.
 *
 * Gdy potrzebny jest {@code Pageable} zgodny z kontraktem Spring Data (0-based),
 * uzyj {@link #toPageable()}.
 */
public class Page implements Pageable
{
    private static final String DEFAULT_SORT = "added";
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int FIRST_PAGE = 1;

    @Setter
    private int page = DEFAULT_PAGE;

    @Setter
    private int size = DEFAULT_SIZE;

    @Setter
    @Getter(AccessLevel.NONE)
    private String sort = DEFAULT_SORT;

    @Setter
    @Getter(AccessLevel.NONE)
    private boolean reverse = true;

    public Page()
    {
    }

    public Page(int page, int size)
    {
        this.page = page;
        this.size = size;
    }

    public Page(int page, int size, Sort sort)
    {
        this.page = page;
        this.size = size;
        sort.get().findFirst().ifPresent(order -> {
            this.sort = order.getProperty();
            this.reverse = order.isDescending();
        });
    }

    public String getSortField()
    {
        return sort;
    }

    public boolean isReverseOrder()
    {
        return reverse;
    }

    /** Numer strony w numeracji 1-based, zgodnie z parametrem zadania. */
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

    @Override
    public Sort getSort()
    {
        return reverse ? Sort.by(sort).descending() : Sort.by(sort).ascending();
    }

    /**
     * Offset liczony od pierwszej strony 1-based. Odziedziczona implementacja
     * liczyla {@code page * size} na stanie nadklasy, czyli zawsze 20.
     */
    @Override
    public long getOffset()
    {
        return (long) (Math.max(page, FIRST_PAGE) - 1) * size;
    }

    /** {@code Pageable} zgodny z kontraktem Spring Data — numeracja 0-based. */
    public Pageable toPageable()
    {
        return PageRequest.of(Math.max(page, FIRST_PAGE) - 1, size, getSort());
    }

    @Override
    public boolean hasPrevious()
    {
        return page > FIRST_PAGE;
    }

    @Override
    public Pageable next()
    {
        return new Page(page + 1, size, getSort());
    }

    @Override
    public Pageable previousOrFirst()
    {
        return hasPrevious() ? new Page(page - 1, size, getSort()) : first();
    }

    @Override
    public Pageable first()
    {
        return new Page(FIRST_PAGE, size, getSort());
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof Page))
        {
            return false;
        }

        final Page that = (Page) other;

        return page == that.page && size == that.size && reverse == that.reverse && Objects.equals(sort, that.sort);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(page, size, sort, reverse);
    }

    @Override
    public String toString()
    {
        return "Page[page=" + page + ", size=" + size + ", sort=" + sort + ", reverse=" + reverse + "]";
    }
}
