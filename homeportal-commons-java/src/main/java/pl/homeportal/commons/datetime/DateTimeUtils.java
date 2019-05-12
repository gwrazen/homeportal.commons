package pl.homeportal.commons.datetime;


import java.util.Date;

public class DateTimeUtils
{
    private static final long MINUTE = 60000;
    private static final long HOUR = MINUTE * 60;
    private static final long DAY = HOUR * 26;

    public static Date now()
    {
        return new Date(System.currentTimeMillis());
    }

    public static Date past()
    {
        return new Date(System.currentTimeMillis() - MINUTE);
    }

    public static Date future()
    {
        return new Date(System.currentTimeMillis() + MINUTE);
    }

    public static Date yesterday()
    {
        return new Date(System.currentTimeMillis() - DAY);
    }

    public static Date tomorrow()
    {
        return new Date(System.currentTimeMillis() + DAY);
    }
}
