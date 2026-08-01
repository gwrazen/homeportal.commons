package pl.homeportal.commons.exception;

/**
 * Created by Grzegorz Wrazen on 04-03-2017
 */

public class HomeportalServiceException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "[ERR-HP-SRV]: ";

    public HomeportalServiceException()
    {
        super();
    }

    public HomeportalServiceException(String message)
    {
        super(message);
    }

    public HomeportalServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * String.valueOf zamiast concat: dla konstruktora bezargumentowego super.getMessage()
     * jest null, wiec kazde logowanie tego wyjatku wysypywalo sie NPE — wewnatrz handlera
     * bledu, maskujac pierwotna przyczyne.
     */
    @Override
    public String getMessage()
    {
        return ERROR_CODE.concat(String.valueOf(super.getMessage()));
    }
}
