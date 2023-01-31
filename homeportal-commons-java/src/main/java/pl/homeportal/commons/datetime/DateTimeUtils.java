package pl.homeportal.commons.datetime;


import javax.validation.constraints.NotNull;
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
        return new Date(currentTimeMillis() - DAY);
    }

    public static Date today()
    {
        return new Date(currentTimeMillis());
    }

    public static Date tomorrow()
    {
        return new Date(currentTimeMillis() + DAY);
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
        return new Date(currentTimeMillis() - (MONTH * months));
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

    public static LocalDateTime toLocalDateTime(@NotNull Date date)
    {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
