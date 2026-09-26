package pl.homeportal.commons.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Re-reads the application properties file and replaces the {@link Reloadable} fields of the
 * configuration bean. Not a Spring bean on purpose — hac and importer do not scan commons.
 */
public class PropertiesReloader
{
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesReloader.class);

    private static final Duration DEFAULT_SETTLE_TIME = Duration.ofSeconds(2);
    private static final String PLACEHOLDER_START = "${";

    private final Object configuration;
    private final Path file;
    private final Clock clock;
    private final Duration settleTime;
    private final Map<String, Field> reloadableFields = new HashMap<>();
    private final Map<String, String> defaults = new HashMap<>();
    private final List<Consumer<ReloadReport>> listeners = new CopyOnWriteArrayList<>();

    // Values the JVM started with: the only reference for "changed in file, needs restart".
    private final Properties startup;
    private Set<String> reportedRestartKeys = new HashSet<>();
    private FileTime lastModified;

    public PropertiesReloader(Object configuration, Path file)
    {
        this(configuration, file, Clock.systemDefaultZone(), DEFAULT_SETTLE_TIME);
    }

    PropertiesReloader(Object configuration, Path file, Clock clock, Duration settleTime)
    {
        this.configuration = configuration;
        this.file = file;
        this.clock = clock;
        this.settleTime = settleTime;
        collectReloadableFields();
        this.lastModified = modificationTime();
        this.startup = readFileQuietly();
    }

    public void addListener(Consumer<ReloadReport> listener)
    {
        listeners.add(listener);
    }

    public Set<String> reloadableKeys()
    {
        return new TreeSet<>(reloadableFields.keySet());
    }

    /**
     * Reloads only when the file modification time moved and the write is older than the settle time.
     */
    public synchronized ReloadReport reloadIfModified()
    {
        final FileTime modified = modificationTime();
        if (modified == null || modified.equals(lastModified))
        {
            return ReloadReport.empty();
        }
        // An editor or scp may still be writing — pick it up on the next pass.
        if (clock.millis() - modified.toMillis() < settleTime.toMillis())
        {
            return ReloadReport.empty();
        }
        return reload();
    }

    /**
     * Reloads unconditionally (manual trigger over JMX).
     */
    public synchronized ReloadReport reload()
    {
        final FileTime modified = modificationTime();
        final Properties current;
        try
        {
            current = readFile();
        }
        catch (IOException e)
        {
            LOG.warn("Configuration reload skipped, cannot read {}: {}", file, e.getMessage());
            return ReloadReport.empty();
        }
        lastModified = modified;

        final List<ReloadReport.Change> changed = new ArrayList<>();
        final List<ReloadReport.Rejection> rejected = new ArrayList<>();
        reloadableFields.forEach((key, field) -> apply(key, field, current, changed, rejected));

        final List<String> requiresRestart = restartKeys(current);
        final ReloadReport report = new ReloadReport(changed, rejected, requiresRestart);
        log(report);

        if (report.hasChanges())
        {
            notifyListeners(report);
        }
        return report;
    }

    private void apply(String key, Field field, Properties current,
                       List<ReloadReport.Change> changed, List<ReloadReport.Rejection> rejected)
    {
        final String raw = current.containsKey(key) ? current.getProperty(key) : defaults.get(key);
        if (raw == null)
        {
            rejected.add(new ReloadReport.Rejection(key, null, "missing in file and no default"));
            return;
        }
        if (raw.contains(PLACEHOLDER_START))
        {
            rejected.add(new ReloadReport.Rejection(key, raw, "placeholders are not reloadable"));
            return;
        }

        final Object value;
        try
        {
            value = convert(raw, field.getType());
        }
        catch (IllegalArgumentException e)
        {
            rejected.add(new ReloadReport.Rejection(key, raw, e.getMessage()));
            return;
        }

        try
        {
            final Object old = field.get(configuration);
            if (!Objects.equals(old, value))
            {
                field.set(configuration, value);
                changed.add(new ReloadReport.Change(key, old, value));
            }
        }
        catch (IllegalAccessException e)
        {
            rejected.add(new ReloadReport.Rejection(key, raw, e.getMessage()));
        }
    }

    private List<String> restartKeys(Properties current)
    {
        final Set<String> keys = new TreeSet<>();
        current.stringPropertyNames().forEach(keys::add);
        startup.stringPropertyNames().forEach(keys::add);
        keys.removeAll(reloadableFields.keySet());
        keys.removeIf(key -> Objects.equals(startup.getProperty(key), current.getProperty(key)));
        return new ArrayList<>(keys);
    }

    private void log(ReloadReport report)
    {
        report.changed().forEach(c -> LOG.info("Configuration reloaded: {} = {} -> {}", c.key(), c.oldValue(), c.newValue()));
        report.rejected().forEach(r -> LOG.warn("Configuration reload rejected: {} = {} ({})", r.key(), r.value(), r.reason()));

        // Warn once per key, not on every later edit of the file.
        final Set<String> pending = new HashSet<>(report.requiresRestart());
        pending.stream()
                .filter(key -> !reportedRestartKeys.contains(key))
                .sorted()
                .forEach(key -> LOG.warn("Configuration changed in file but requires restart: {}", key));
        reportedRestartKeys = pending;
    }

    private void notifyListeners(ReloadReport report)
    {
        for (Consumer<ReloadReport> listener : listeners)
        {
            try
            {
                listener.accept(report);
            }
            catch (RuntimeException e)
            {
                LOG.error("Configuration reload listener failed", e);
            }
        }
    }

    private void collectReloadableFields()
    {
        for (Class<?> type = configuration.getClass(); type != null && type != Object.class; type = type.getSuperclass())
        {
            for (Field field : type.getDeclaredFields())
            {
                final Value value = field.getAnnotation(Value.class);
                if (!field.isAnnotationPresent(Reloadable.class) || value == null)
                {
                    continue;
                }
                final String expression = value.value();
                if (!expression.startsWith(PLACEHOLDER_START) || !expression.endsWith("}"))
                {
                    throw new IllegalStateException("@Reloadable needs @Value(\"${key:default}\"): " + field);
                }
                final String body = expression.substring(PLACEHOLDER_START.length(), expression.length() - 1);
                final int separator = body.indexOf(':');
                final String key = separator < 0 ? body : body.substring(0, separator);
                field.setAccessible(true);
                reloadableFields.put(key, field);
                defaults.put(key, separator < 0 ? null : body.substring(separator + 1));
            }
        }
    }

    // Mirrors Spring: Properties.load(InputStream) reads ISO-8859-1, number and boolean values are trimmed.
    static Object convert(String raw, Class<?> type)
    {
        if (type == String.class)
        {
            return raw;
        }
        final String value = raw.trim();
        try
        {
            if (type == int.class || type == Integer.class)
            {
                return Integer.valueOf(value);
            }
            if (type == long.class || type == Long.class)
            {
                return Long.valueOf(value);
            }
            if (type == double.class || type == Double.class)
            {
                return Double.valueOf(value);
            }
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("not a number");
        }
        if (type == boolean.class || type == Boolean.class)
        {
            return toBoolean(value);
        }
        throw new IllegalArgumentException("unsupported type " + type.getSimpleName());
    }

    private static Boolean toBoolean(String value)
    {
        switch (value.toLowerCase())
        {
            case "true", "on", "yes", "1":
                return Boolean.TRUE;
            case "false", "off", "no", "0":
                return Boolean.FALSE;
            default:
                throw new IllegalArgumentException("not a boolean");
        }
    }

    private FileTime modificationTime()
    {
        try
        {
            return Files.getLastModifiedTime(file);
        }
        catch (IOException e)
        {
            return null;
        }
    }

    private Properties readFile() throws IOException
    {
        final Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(file))
        {
            properties.load(in);
        }
        return properties;
    }

    private Properties readFileQuietly()
    {
        try
        {
            return readFile();
        }
        catch (IOException e)
        {
            LOG.warn("Configuration file not readable at startup: {}", file);
            return new Properties();
        }
    }
}
