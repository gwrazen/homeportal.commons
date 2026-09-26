package pl.homeportal.commons.configuration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Outcome of one {@link PropertiesReloader} pass.
 *
 * @param changed         reloadable values replaced in memory
 * @param rejected        reloadable values left untouched because the file value is invalid
 * @param requiresRestart keys whose file value differs from the one loaded at startup but are not reloadable
 */
public record ReloadReport(List<Change> changed, List<Rejection> rejected, List<String> requiresRestart)
{
    public record Change(String key, Object oldValue, Object newValue)
    {
    }

    public record Rejection(String key, String value, String reason)
    {
    }

    public static ReloadReport empty()
    {
        return new ReloadReport(List.of(), List.of(), List.of());
    }

    public boolean hasChanges()
    {
        return !changed.isEmpty();
    }

    public Set<String> changedKeys()
    {
        return changed.stream().map(Change::key).collect(Collectors.toSet());
    }

    @Override
    public String toString()
    {
        if (changed.isEmpty() && rejected.isEmpty() && requiresRestart.isEmpty())
        {
            return "no changes";
        }

        final StringBuilder report = new StringBuilder();
        changed.forEach(c -> report.append(format("changed: %s = %s -> %s%n", c.key(), c.oldValue(), c.newValue())));
        rejected.forEach(r -> report.append(format("rejected: %s = %s (%s)%n", r.key(), r.value(), r.reason())));
        requiresRestart.forEach(key -> report.append(format("requires restart: %s%n", key)));
        return report.toString().trim();
    }
}
