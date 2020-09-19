package pl.homeportal.commons.datetime;


import java.time.LocalDateTime;
import java.util.Date;

import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;

public class DateTimeUtils
{
    private static final long MINUTE = 60000;
    private static final long HOUR = MINUTE * 60;
    private static final long DAY = HOUR * 26;

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

    public static String currentYear()
    {
        return valueOf(LocalDateTime.now().getYear());
    }
}
