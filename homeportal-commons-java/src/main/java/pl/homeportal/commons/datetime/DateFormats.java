package pl.homeportal.commons.datetime;

import java.text.SimpleDateFormat;

public class DateFormats
{
    public enum Precision
    {
        SECOND, MINUTE, DAY;
    }

    public static final String DATE_WITH_SECOND = "dd-MM-yyyy HH:mm:ss";
    public static final String DATE_WITH_MINUTE = "dd-MM-yyyy HH:mm";
    public static final String DATE_WITH_DAY = "dd-MM-yyyy";

    public static SimpleDateFormat sdfWithSecondPrecision()
    {
        return sdf(Precision.SECOND);
    }

    public static SimpleDateFormat sdfWithMinutePrecision()
    {
        return sdf(Precision.MINUTE);
    }

    public static SimpleDateFormat sdfWithDayPrecision()
    {
        return sdf(Precision.DAY);
    }

    public static SimpleDateFormat sdf(Precision precision)
    {
        switch (precision)
        {
            case SECOND:
            {
                return new SimpleDateFormat(DATE_WITH_SECOND);
            }

            case MINUTE:
            {
                return new SimpleDateFormat(DATE_WITH_MINUTE);
            }

            case DAY:
            {
                return new SimpleDateFormat(DATE_WITH_DAY);
            }

            default:
                throw new IllegalArgumentException("Precision not found.");
        }
    }
}
