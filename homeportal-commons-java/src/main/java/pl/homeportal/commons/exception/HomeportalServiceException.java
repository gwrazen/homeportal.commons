package pl.homeportal.commons.exception;

import lombok.Value;

/**
 * Created by Grzegorz Wrażeń on 04.03.2017.
 */
@Value
public class HomeportalServiceException extends RuntimeException
{
    public HomeportalServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public HomeportalServiceException(String message)
    {
        super(message);
    }
}
