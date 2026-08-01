package pl.homeportal.commons.datetime;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static pl.homeportal.commons.datetime.DateTimeUtils.toLocalDate;

public class DateTimeUtilsTest
{
    /**
     * Regresja: todayPlusMonths liczylo na stalej MONTH = 31 dni, a todayPlusYears
     * i todayMinusYears na YEAR = 372 dni. Dodanie i odjecie tego samego okresu
     * nie wracalo wiec do punktu wyjscia, a rok "myllil sie" o tydzien.
     */
    @Test
    public void plusAndMinusMonthsAreInverse()
    {
        // given
        final LocalDate today = LocalDate.now();

        // when
        final Date plus = DateTimeUtils.todayPlusMonths(1);
        final Date minus = DateTimeUtils.todayMinusMonths(1);

        // then
        assertEquals(today.plusMonths(1), toLocalDate(plus));
        assertEquals(today.minusMonths(1), toLocalDate(minus));
    }

    @Test
    public void plusAndMinusYearsFollowTheCalendar()
    {
        // given
        final LocalDate today = LocalDate.now();

        // when
        final Date plus = DateTimeUtils.todayPlusYears(1);
        final Date minus = DateTimeUtils.todayMinusYears(1);

        // then
        assertEquals(today.plusYears(1), toLocalDate(plus));
        assertEquals(today.minusYears(1), toLocalDate(minus));
    }

    @Test
    public void tenYearsBackDoesNotDrift()
    {
        assertEquals(LocalDate.now().minusYears(10), toLocalDate(DateTimeUtils.todayMinusYears(10)));
    }

    @Test
    public void daysUseTheCalendarAsBefore()
    {
        assertEquals(LocalDate.now().plusDays(2), toLocalDate(DateTimeUtils.dayAfterTomorrow()));
        assertEquals(LocalDate.now().minusDays(1), toLocalDate(DateTimeUtils.yesterday()));
    }
}
