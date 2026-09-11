package pl.homeportal.commons.datetime;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DateFormatsTest
{

    @Test
    public void timeWithSecondWithoutHourPrecision()
    {
        SimpleDateFormat format = DateFormats.timeWithSecondPrecision();
        String time = format.format(new Date());
        assertEquals(DateFormats.TIME_WITH_SECOND, format.toPattern());
        assertTrue( time.matches("\\d{2}:\\d{2}:\\d{2}"),"Unexpected time format: " + time);
    }

    @Test
    public void timeWithMinutePrecision()
    {
        SimpleDateFormat format = DateFormats.timeWithMinutePrecision();
        String time = format.format(new Date());
        assertEquals(DateFormats.TIME_WITH_MINUTE, format.toPattern());
        assertTrue( time.matches("\\d{2}:\\d{2}"),"Unexpected time format: " + time);
    }

    @Test
    public void datetimeWithDayPrecision()
    {
        SimpleDateFormat format = DateFormats.datetimeWithDayPrecision();
        String date = format.format(new Date());
        assertEquals(DateFormats.DATE_WITH_DAY, format.toPattern());
        assertTrue( date.matches("\\d{2}-\\d{2}-\\d{4}"),"Unexpected date format: " + date);
    }

    @Test
    public void sdfTimeRejectsDayPrecision()
    {
        assertThrows(IllegalArgumentException.class, () ->
        {
            DateFormats.sdfTime(DateFormats.Precision.DAY);

        });
    }
}
