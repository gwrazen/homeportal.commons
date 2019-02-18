package pl.homeportal.commons.datetime;

import org.springframework.stereotype.Component;

/**
 * Created by gwrazen on 31/08/2015.
 */
@Component
public class Timer
{
    private long start;
    private long end;

    public void start()
    {
        start = System.currentTimeMillis();
        end = 0;
    }

    public void end()
    {
        end = System.currentTimeMillis();
    }

    public String summary()
    {
        float taken = (end - start)/1000f;
        return taken + "s";
    }
}
