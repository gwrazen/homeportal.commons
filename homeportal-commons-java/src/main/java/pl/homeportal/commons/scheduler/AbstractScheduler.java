package pl.homeportal.commons.scheduler;

import java.util.Date;

import static java.lang.String.format;
import static pl.homeportal.commons.datetime.DateFormats.timeWithSecondPrecision;
import static pl.homeportal.commons.datetime.DateTimeUtils.now;

/**
 * Created by Grzegorz Wrazen on 09-10-2020
 */

public abstract class AbstractScheduler
{
    private static final String STARTED_MESSAGE = "%s scheduler enabled. Scheduler started at %s.";
    private static final String ENDED_MESSAGE = "%s scheduler enabled. Scheduler ended at %s.";
    private static final String DISABLED_MESSAGE = "%s scheduler disabled.";

    protected String jobName;

    public abstract boolean isEnabled();

    public abstract void job();

    protected String start()
    {
        return format(STARTED_MESSAGE, jobName, timeWithSecondPrecision().format(now()));
    }

    protected String end()
    {
        return format(ENDED_MESSAGE, jobName, timeWithSecondPrecision().format(now()));
    }

    protected String disabled()
    {
        return format(DISABLED_MESSAGE, jobName);
    }
}
