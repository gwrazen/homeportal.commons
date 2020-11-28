package pl.homeportal.commons.service;

import org.junit.Test;
import org.slf4j.Logger;

import static java.util.Arrays.asList;
import static pl.homeportal.commons.service.LoggingSupport.logger;

public class LoggingSupportTest
{
    @Test
    public void information()
    {
        final Logger logger = logger(LoggingSupportTest.class);
        final String testMessage = "Test message with param: %s";
        LoggingSupport.information(logger, testMessage, asList("Some param"));
    }
}