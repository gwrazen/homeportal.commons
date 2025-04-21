package pl.homeportal.commons.datetime;


import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;

public class DateTimeUtils
{
    private static final long MINUTE = 60000;
    private static final long HOUR = MINUTE * 60;
    private static final long DAY = HOUR * 24;
    private static final long MONTH = DAY * 31;
    private static final long YEAR = MONTH * 12;

    public static Date now()
    {
        return new Date(currentTimeMillis());
    }

    public static Date past()
    {
        return new Date(currentTimeMillis() - MINUTE);
    }

    public static Date future()
    {
        return new Date(currentTimeMillis() + MINUTE);
    }

    public static Date yesterday()
    {
        return toDate(LocalDate.now().minusDays(1));
    }

    public static Date today()
    {
        return toDate(LocalDate.now());
    }

    public static Date tomorrow()
    {
        return toDate(LocalDate.now().plusDays(1));
    }

    public static Date dayAfterTomorrow()
    {
        return toDate(LocalDate.now().plusDays(2));
    }

    public static Date todayMinusDays(int days)
    {
        return new Date(currentTimeMillis() - (DAY * days));
    }

    public static Date todayPlusDays(int days)
    {
        return new Date(currentTimeMillis() + (DAY * days));
    }

    public static Date todayMinusMonths(int months)
    {
        return toDate(LocalDateTime.now().minusMonths(months));
    }

    public static Date todayPlusMonths(int months)
    {
        return new Date(currentTimeMillis() + (MONTH * months));
    }

    public static Date todayMinusYears(int years)
    {
        return new Date(currentTimeMillis() - (YEAR * years));
    }

    public static Date todayPlusYears(int years)
    {
        return new Date(currentTimeMillis() + (YEAR * years));
    }

    public static String currentYear()
    {
        return valueOf(LocalDateTime.now().getYear());
    }

    public static Date toDate(@NotNull LocalDateTime dateTime)
    {
        return Date.from(dateTime
                   .atZone(ZoneId.systemDefault())
                   .toInstant());
    }

    public static Date toDate(@NotNull LocalDate date)
    {
        return Date.from(date
                   .atStartOfDay(ZoneId.systemDefault())
                   .toInstant());
    }

    public static LocalDateTime toLocalDateTime(@NotNull Date date)
    {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static LocalDate toLocalDate(@NotNull Date date)
    {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
