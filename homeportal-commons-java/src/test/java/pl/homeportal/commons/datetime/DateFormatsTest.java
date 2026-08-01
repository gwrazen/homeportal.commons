package pl.homeportal.commons.datetime;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

public class DateFormatsTest
{

    @Test
    public void timeWithSecondWithoutHourPrecision()
    {
        SimpleDateFormat format = DateFormats.timeWithSecondPrecision();
        String time = format.format(new Date());
        assertEquals(DateFormats.TIME_WITH_SECOND, format.toPattern());
        assertTrue("Unexpected time format: " + time, time.matches("\\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    public void timeWithMinutePrecision()
    {
        SimpleDateFormat format = DateFormats.timeWithMinutePrecision();
        String time = format.format(new Date());
        assertEquals(DateFormats.TIME_WITH_MINUTE, format.toPattern());
        assertTrue("Unexpected time format: " + time, time.matches("\\d{2}:\\d{2}"));
    }

    @Test
    public void datetimeWithDayPrecision()
    {
        SimpleDateFormat format = DateFormats.datetimeWithDayPrecision();
        String date = format.format(new Date());
        assertEquals(DateFormats.DATE_WITH_DAY, format.toPattern());
        assertTrue("Unexpected date format: " + date, date.matches("\\d{2}-\\d{2}-\\d{4}"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void sdfTimeRejectsDayPrecision()
    {
        DateFormats.sdfTime(DateFormats.Precision.DAY);
    }
}
