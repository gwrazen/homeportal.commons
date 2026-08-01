package pl.homeportal.commons.data.index;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        assertFalse("Drugie pozyskanie musi zostac odrzucone", monitor.acquireLock("jmx"));
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
