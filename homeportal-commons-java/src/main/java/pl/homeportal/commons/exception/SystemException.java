package pl.homeportal.commons.exception;

/**
 * Created by grzechu on 04.03.2017.
 */
public class SystemException extends RuntimeException
{
    private final String message;
    private final Throwable throwable;

    public SystemException()
    {
        this.message = null;
        this.throwable = null;
    }

    public SystemException(String message)
    {
        this.message = message;
        throwable = null;
    }

    public SystemException(String message, Throwable throwable)
    {
        this.message = message;
        this.throwable = throwable;
    }
}
