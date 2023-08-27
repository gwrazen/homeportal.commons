package pl.homeportal.commons.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;
import static pl.homeportal.commons.text.Constants.EMPTY_STRING;

/**
 * Created by Grzegorz Wrazen on 04-03-2017
 */

public class HomeportalServiceException extends RuntimeException
{
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

    @Override
    public String getMessage()
    {
        return ERROR_CODE.concat(super.getMessage());
    }
}