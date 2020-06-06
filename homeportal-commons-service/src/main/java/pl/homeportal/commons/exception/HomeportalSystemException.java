package pl.homeportal.commons.exception;

import lombok.Value;

/**
 * Created by Grzegorz Wrażeń on 04.03.2017.
 */
@Value
public class HomeportalSystemException extends RuntimeException
{
    private final String message;
    private final Throwable throwable;

    public HomeportalSystemException(Throwable throwable)
    {
        this.message = null;
        this.throwable = throwable;
    }

    public HomeportalSystemException(String message)
    {
        this.message = message;
        throwable = null;
    }

    public HomeportalSystemException(String message, Throwable throwable)
    {
        this.message = message;
        this.throwable = throwable;
    }
}
