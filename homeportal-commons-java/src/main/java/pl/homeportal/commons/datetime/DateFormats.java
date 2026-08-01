package pl.homeportal.commons.datetime;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateFormats
{
    public enum Precision
    {
        SECOND, MINUTE, DAY;
    }

    public static final String TIME_WITH_SECOND  = "HH:mm:ss";
    public static final String TIME_WITH_MINUTE  = "HH:mm";

    public static final String DATE_WITH_SECOND = "dd-MM-yyyy HH:mm:ss";
    public static final String DATE_WITH_MINUTE = "dd-MM-yyyy HH:mm";
    public static final String DATE_WITH_DAY    = "dd-MM-yyyy";

    public static SimpleDateFormat timeWithSecondPrecision()
    {
        return sdfTime(Precision.SECOND);
    }

    public static SimpleDateFormat timeWithMinutePrecision()
    {
        return sdfTime(Precision.MINUTE);
    }

    public static SimpleDateFormat datetimeWithSecondPrecision()
    {
        return sdfDatetime(Precision.SECOND);
    }

    public static SimpleDateFormat datetimeWithMinutePrecision()
    {
        return sdfDatetime(Precision.MINUTE);
    }

    public static SimpleDateFormat datetimeWithDayPrecision()
    {
        return sdfDatetime(Precision.DAY);
    }

    public static SimpleDateFormat sdfDatetime(Precision precision)
    {
        switch (precision)
        {
            case SECOND:
            {
                return new SimpleDateFormat(DATE_WITH_SECOND, Locale.ROOT);
            }

            case MINUTE:
            {
                return new SimpleDateFormat(DATE_WITH_MINUTE, Locale.ROOT);
            }

            case DAY:
            {
                return new SimpleDateFormat(DATE_WITH_DAY, Locale.ROOT);
            }

            default:
                throw new IllegalArgumentException("Precision not found.");
        }
    }

    public static SimpleDateFormat sdfTime(Precision precision)
    {
        switch (precision)
        {
            case SECOND:
            {
                return new SimpleDateFormat(TIME_WITH_SECOND, Locale.ROOT);
            }

            case MINUTE:
            {
                return new SimpleDateFormat(TIME_WITH_MINUTE, Locale.ROOT);
            }

            default:
                throw new IllegalArgumentException("Precision not found.");
        }
    }
}
