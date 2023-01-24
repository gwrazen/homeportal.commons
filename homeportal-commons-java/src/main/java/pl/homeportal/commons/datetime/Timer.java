package pl.homeportal.commons.datetime;

import static java.lang.System.currentTimeMillis;

/**
 * Created by Grzegorz Wrazen on 31-08-2015
 */

public class Timer
{
    private long start;
    private long end;

    public void start()
    {
        start = currentTimeMillis();
    }

    public void end()
    {
        end = currentTimeMillis();
    }

    public String summary()
    {
        end = currentTimeMillis();
        float taken = (end - start) / 1000f;
        return taken + "s";
    }

    public static Timer of()
    {
        final Timer timer = new Timer();
        timer.start();
        return timer;
    }
}
