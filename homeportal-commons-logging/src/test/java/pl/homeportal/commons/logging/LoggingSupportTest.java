package pl.homeportal.commons.logging;

import org.junit.Test;
import org.slf4j.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static pl.homeportal.commons.logging.LoggingSupport.logger;
import static pl.homeportal.commons.logging.LoggingSupport.shortName;

public class LoggingSupportTest
{
    @Test
    public void loggerIsNamedAfterTheClassShortName()
    {
        final Logger logger = logger(LoggingSupportTest.class);
        assertNotNull(logger);
        assertEquals(LoggingSupportTest.class.getSimpleName(), logger.getName());
    }

    @Test
    public void loggerByNameKeepsTheGivenName()
    {
        final Logger logger = logger("custom-logger");
        assertEquals("custom-logger", logger.getName());
    }

    @Test
    public void shortNameStripsThePackage()
    {
        assertEquals("LoggingSupportTest", shortName(LoggingSupportTest.class));
    }

    @Test
    public void informationWithTemplateFormatsWithoutThrowing()
    {
        final Logger logger = logger(LoggingSupportTest.class);
        LoggingSupport.information(logger, "Test message with param: %s", "Some param");
    }
}
