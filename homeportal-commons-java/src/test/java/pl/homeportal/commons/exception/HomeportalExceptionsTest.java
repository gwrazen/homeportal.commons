package pl.homeportal.commons.exception;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HomeportalExceptionsTest
{
    /**
     * Regresja: ERROR_CODE.concat(super.getMessage()) rzucalo NPE dla konstruktora
     * bezargumentowego — i robilo to wewnatrz handlera bledu, wiec pierwotna
     * przyczyna nigdy nie trafiala do logu.
     */
    @Test
    public void serviceExceptionWithoutMessageDoesNotThrowOnGetMessage()
    {
        assertNotNull(new HomeportalServiceException().getMessage());
    }

    @Test
    public void serviceExceptionKeepsItsMessage()
    {
        assertTrue(new HomeportalServiceException("boom").getMessage().endsWith("boom"));
    }

    @Test
    public void validationExceptionWithoutMessageDoesNotThrowOnGetMessage()
    {
        assertNotNull(new HomeportalValidationException((String) null).getMessage());
    }

    @Test
    public void securityExceptionDescribesTheAttempt()
    {
        final String message = new HomeportalSecurityException("jan", "AGENT", "DELETE_AGENCY", "42").getMessage();

        assertTrue(message.contains("jan"));
        assertTrue(message.contains("DELETE_AGENCY"));
    }

    @Test
    public void securityExceptionCarriesCause()
    {
        final Exception cause = new IllegalStateException("no algorithm");
        final HomeportalSecurityException exception = new HomeportalSecurityException("MD5 unavailable", cause);

        assertTrue(exception.getMessage().contains("MD5 unavailable"));
        assertNotNull(exception.getCause());
    }
}
