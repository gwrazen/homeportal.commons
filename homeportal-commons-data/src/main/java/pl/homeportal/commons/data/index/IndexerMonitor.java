package pl.homeportal.commons.data.index;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Boolean.FALSE;

@Component
public class IndexerMonitor
{
    public static final Object MONITOR = new Object();

    private AtomicBoolean running = new AtomicBoolean(FALSE);

    @Getter
    private String lockOwner;

    public boolean isRunning()
    {
        return running.get();
    }

    public void acquireLock(String lockOwner)
    {
        synchronized (MONITOR)
        {
            this.running.set(Boolean.TRUE);
            this.lockOwner = lockOwner;
        }
    }

    public void releaseLock()
    {
        synchronized (MONITOR)
        {
            this.running.set(FALSE);
            this.lockOwner = null;
        }
    }
}
