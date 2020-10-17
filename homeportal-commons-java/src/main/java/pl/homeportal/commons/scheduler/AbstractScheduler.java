package pl.homeportal.commons.scheduler;

import java.util.Date;

import static java.lang.String.format;
import static pl.homeportal.commons.datetime.DateFormats.timeWithSecondPrecision;

/**
 * Created by Grzegorz Wrazen on 09-10-2020
 */

public abstract class AbstractScheduler
{
    private static final String STARTED_MESSAGE = "%s scheduler enabled. Scheduler started at %s.";
    private static final String ENDED_MESSAGE = "%s scheduler enabled. Scheduler ended at %s.";
    private static final String DISABLED_MESSAGE = "%s scheduler disabled.";

    public abstract void job();

    protected String start(String schedulerName, Date date)
    {
        return format(STARTED_MESSAGE, schedulerName, timeWithSecondPrecision().format(date));
    }

    protected String end(String schedulerName, Date date)
    {
        return format(ENDED_MESSAGE, schedulerName, timeWithSecondPrecision().format(date));
    }

    protected String disabled(String schedulerName)
    {
        return format(DISABLED_MESSAGE, schedulerName);
    }
}
