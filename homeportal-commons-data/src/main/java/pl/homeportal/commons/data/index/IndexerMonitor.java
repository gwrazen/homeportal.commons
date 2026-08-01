package pl.homeportal.commons.data.index;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Boolean.FALSE;

@Component
public class IndexerMonitor
{
    private static final String NONE = "none";

    private final AtomicBoolean running = new AtomicBoolean(FALSE);

    @Getter
    private String lockOwner = NONE;

    public boolean isRunning()
    {
        return running.get();
    }

    /**
     * @return true, gdy blokada zostala pozyskana; false, gdy indeksowanie juz trwa.
     *
     * Wczesniej metoda ustawiala flage bezwarunkowo i nie zwracala niczego — czyli
     * nie byla blokada, tylko znacznikiem stanu. Scheduler i JMX mogly ruszyc
     * z reindeksem rownolegle, a pierwszy releaseLock zwalnial flage w trakcie
     * pracy drugiego.
     */
    public boolean acquireLock(String lockOwner)
    {
        if (!running.compareAndSet(FALSE, Boolean.TRUE))
        {
            return false;
        }

        this.lockOwner = lockOwner;

        return true;
    }

    public void releaseLock()
    {
        this.lockOwner = NONE;
        this.running.set(FALSE);
    }
}
