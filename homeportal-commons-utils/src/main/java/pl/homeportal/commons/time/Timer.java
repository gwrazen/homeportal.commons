package pl.homeportal.commons.time;

import lombok.Builder;

/**
 * Created by gwrazen on 31/08/2015.
 */
@Builder
public class Timer
{
    private long start;
    private long end;

    public void start()
    {
        start = System.currentTimeMillis();
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
