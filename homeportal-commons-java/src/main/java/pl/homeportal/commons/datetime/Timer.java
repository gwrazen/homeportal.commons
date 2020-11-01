package pl.homeportal.commons.datetime;

import lombok.NoArgsConstructor;

import static java.lang.System.currentTimeMillis;

/**
 * Created by Grzegorz Wrazen on 31-08-2015
 */

@NoArgsConstructor(staticName = "of")
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
        float taken = (end - start) / 1000f;
        return taken + "s";
    }
}
