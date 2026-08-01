package pl.homeportal.commons.datetime;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimerTest
{
    /**
     * Regresja: summary() nadpisywalo pole `end`, wiec pomiar jawnie zamkniety
     * przez end() raportowal takze czas tego, co dzialo sie po nim.
     */
    @Test
    public void summaryDoesNotExtendAClosedMeasurement() throws Exception
    {
        // given
        final Timer timer = Timer.of();
        Thread.sleep(30);
        timer.end();
        final String afterEnd = timer.summary();

        // when — praca wykonana juz po zamknieciu pomiaru
        Thread.sleep(60);

        // then
        assertEquals(afterEnd, timer.summary());
    }

    @Test
    public void summaryWithoutEndMeasuresUntilNow() throws Exception
    {
        // given
        final Timer timer = Timer.of();

        // when
        Thread.sleep(10);

        // then — brak wyjatku i sensowny format
        org.junit.Assert.assertTrue(timer.summary().endsWith("s"));
    }
}
