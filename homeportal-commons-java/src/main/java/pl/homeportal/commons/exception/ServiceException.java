package pl.homeportal.commons.exception;

/**
 * Created by grzechu on 04.03.2017.
 */
public class ServiceException extends SystemException
{
    public ServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ServiceException(String message)
    {
        super(message);
    }
}
