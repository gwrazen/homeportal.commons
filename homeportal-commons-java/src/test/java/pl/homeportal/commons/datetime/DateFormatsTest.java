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
        System.out.println(time);
    }
}