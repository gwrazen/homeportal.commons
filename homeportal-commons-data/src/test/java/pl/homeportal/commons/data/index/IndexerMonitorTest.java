package pl.homeportal.commons.data.index;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexerMonitorTest
{
    /**
     * Regresja: acquireLock ustawial flage bezwarunkowo i nie zwracal niczego, wiec
     * scheduler i JMX mogly ruszyc z reindeksem rownolegle na tym samym indeksie.
     */
    @Test
    public void secondAcquisitionIsRejectedWhileIndexingRuns()
    {
        final IndexerMonitor monitor = new IndexerMonitor();

        assertTrue(monitor.acquireLock("scheduler"));
        assertFalse( monitor.acquireLock("jmx"),"Drugie pozyskanie musi zostac odrzucone");
        assertEquals("scheduler", monitor.getLockOwner());
        assertTrue(monitor.isRunning());
    }

    @Test
    public void lockCanBeAcquiredAgainAfterRelease()
    {
        final IndexerMonitor monitor = new IndexerMonitor();
        monitor.acquireLock("scheduler");
        monitor.releaseLock();

        assertFalse(monitor.isRunning());
        assertEquals("none", monitor.getLockOwner());
        assertTrue(monitor.acquireLock("jmx"));
    }
}
