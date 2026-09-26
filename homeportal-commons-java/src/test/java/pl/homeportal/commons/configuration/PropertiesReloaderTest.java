package pl.homeportal.commons.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertiesReloaderTest
{
    private static final Duration SETTLE = Duration.ofSeconds(2);
    private static final Instant START = Instant.parse("2026-09-26T08:00:00Z");

    @TempDir
    Path directory;

    private Path file;
    private MutableClock clock;
    private TestConfiguration configuration;

    @BeforeEach
    public void setUp() throws IOException
    {
        file = directory.resolve("app.properties");
        clock = new MutableClock(START);
        configuration = new TestConfiguration();
        write("limit = 45\nenabled = true\nversion = 51\nport = 8080\n", START);
    }

    @Test
    public void testUnchangedFileGivesEmptyReport()
    {
        // given
        PropertiesReloader reloader = reloader();
        clock.plus(Duration.ofMinutes(5));

        // when
        ReloadReport report = reloader.reloadIfModified();

        // then
        assertEquals("no changes", report.toString());
        assertEquals(45, configuration.limit);
    }

    @Test
    public void testReloadableValueIsReplaced() throws IOException
    {
        // given
        PropertiesReloader reloader = reloader();
        modify("limit = 50\nenabled = off\nversion = 52\nport = 8080\n");

        // when
        ReloadReport report = reloader.reloadIfModified();

        // then
        assertEquals(50, configuration.limit);
        assertFalse(configuration.enabled);
        assertEquals("52", configuration.version);
        assertEquals(3, report.changed().size());
        assertTrue(report.changedKeys().contains("limit"));
    }

    @Test
    public void testNonReloadableChangeRequiresRestart() throws IOException
    {
        // given
        PropertiesReloader reloader = reloader();
        modify("limit = 45\nenabled = true\nversion = 51\nport = 9090\n");

        // when
        ReloadReport report = reloader.reloadIfModified();

        // then
        assertEquals(List.of("port"), report.requiresRestart());
        assertEquals(8080, configuration.port);
        assertFalse(report.hasChanges());
    }

    @Test
    public void testInvalidValueKeepsOldOne() throws IOException
    {
        // given
        PropertiesReloader reloader = reloader();
        modify("limit = fifty\nenabled = true\nversion = 51\nport = 8080\n");

        // when
        ReloadReport report = reloader.reloadIfModified();

        // then
        assertEquals(45, configuration.limit);
        assertEquals("limit", report.rejected().get(0).key());
    }

    @Test
    public void testRemovedKeyFallsBackToDefault() throws IOException
    {
        // given
        PropertiesReloader reloader = reloader();
        modify("enabled = true\nversion = 51\nport = 8080\n");

        // when
        reloader.reloadIfModified();

        // then
        assertEquals(30, configuration.limit);
    }

    @Test
    public void testFreshWriteWaitsForSettleTime() throws IOException
    {
        // given
        PropertiesReloader reloader = reloader();
        clock.plus(Duration.ofMinutes(1));
        write("limit = 50\nenabled = true\nversion = 51\nport = 8080\n", clock.instant());

        // when
        ReloadReport tooEarly = reloader.reloadIfModified();
        clock.plus(SETTLE);
        ReloadReport settled = reloader.reloadIfModified();

        // then
        assertFalse(tooEarly.hasChanges());
        assertTrue(settled.hasChanges());
        assertEquals(50, configuration.limit);
    }

    @Test
    public void testManualReloadIgnoresModificationTime() throws IOException
    {
        // given
        PropertiesReloader reloader = reloader();
        write("limit = 60\nenabled = true\nversion = 51\nport = 8080\n", START);

        // when
        ReloadReport report = reloader.reload();

        // then
        assertTrue(report.hasChanges());
        assertEquals(60, configuration.limit);
    }

    @Test
    public void testFailingListenerDoesNotStopOthers() throws IOException
    {
        // given
        PropertiesReloader reloader = reloader();
        List<ReloadReport> received = new ArrayList<>();
        reloader.addListener(report -> {
            throw new IllegalStateException("boom");
        });
        reloader.addListener(received::add);
        modify("limit = 50\nenabled = true\nversion = 51\nport = 8080\n");

        // when
        reloader.reloadIfModified();

        // then
        assertEquals(1, received.size());
    }

    @Test
    public void testPlaceholderIsRejected() throws IOException
    {
        // given
        PropertiesReloader reloader = reloader();
        modify("limit = 45\nenabled = true\nversion = ${other}\nport = 8080\n");

        // when
        ReloadReport report = reloader.reloadIfModified();

        // then
        assertEquals("51", configuration.version);
        assertEquals("version", report.rejected().get(0).key());
    }

    @Test
    public void testReloadableWithoutPlaceholderFailsFast()
    {
        // when / then
        assertThrows(IllegalStateException.class,
                () -> new PropertiesReloader(new BrokenConfiguration(), file, clock, SETTLE));
    }

    private PropertiesReloader reloader()
    {
        return new PropertiesReloader(configuration, file, clock, SETTLE);
    }

    private void modify(String content) throws IOException
    {
        clock.plus(Duration.ofMinutes(1));
        write(content, clock.instant().minus(SETTLE));
    }

    private void write(String content, Instant modified) throws IOException
    {
        Files.writeString(file, content);
        Files.setLastModifiedTime(file, FileTime.from(modified));
    }

    static class TestConfiguration
    {
        @Reloadable
        @Value("${limit:30}")
        volatile int limit = 45;

        @Reloadable
        @Value("${enabled:false}")
        volatile boolean enabled = true;

        @Reloadable
        @Value("${version:1}")
        volatile String version = "51";

        @Value("${port:8080}")
        int port = 8080;
    }

    static class BrokenConfiguration
    {
        @Reloadable
        @Value("literal")
        String value;
    }

    static class MutableClock extends Clock
    {
        private Instant now;

        MutableClock(Instant now)
        {
            this.now = now;
        }

        void plus(Duration duration)
        {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone()
        {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone)
        {
            return this;
        }

        @Override
        public Instant instant()
        {
            return now;
        }
    }
}
