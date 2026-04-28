package pl.homeportal.commons.data;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

@RequiredArgsConstructor(staticName = "of")
public class SimpleTimeCache<T>
{
    private static final Logger LOG = Logger.getLogger(SimpleTimeCache.class.getName());

    private final TimeUnit unit;
    private final int value;
    private final boolean controlTime;

    private volatile LocalDateTime begin = null;
    private volatile T data;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public T getData()
    {
        lock.readLock().lock();
        try
        {
            return data;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public void setData(T data)
    {
        lock.writeLock().lock();
        try
        {
            if (begin == null || !controlTime || isExpired())
            {
                this.data = data;
                this.begin = LocalDateTime.now();
                LOG.info("Cache reloaded with data: " + this.data);
            }
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public boolean isExpired()
    {
        if (begin == null)
        {
            return true;
        }
        final LocalDateTime now = LocalDateTime.now();
        switch (unit)
        {
            case SECONDS:
                return !begin.plusSeconds(value).isAfter(now);
            case MINUTES:
                return !begin.plusMinutes(value).isAfter(now);
            case HOURS:
                return !begin.plusHours(value).isAfter(now);
            default:
                throw new IllegalArgumentException("Invalid time unit: " + unit);
        }
    }
}