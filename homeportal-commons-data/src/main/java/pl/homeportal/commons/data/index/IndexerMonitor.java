package pl.homeportal.commons.data.index;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Boolean.FALSE;

@Component
public class IndexerMonitor
{
    public static final Object MONITOR = new Object();

    private AtomicBoolean running = new AtomicBoolean(FALSE);

    public boolean isRunning()
    {
        return running.get();
    }

    public void acquireLock()
    {
        synchronized (MONITOR)
        {
            running.set(Boolean.TRUE);
        }
    }

    public void releaseLock()
    {
        synchronized (MONITOR)
        {
            running.set(FALSE);
        }
    }
}
