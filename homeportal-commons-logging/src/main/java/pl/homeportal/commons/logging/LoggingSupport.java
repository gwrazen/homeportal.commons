package pl.homeportal.commons.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.homeportal.commons.data.entity.Identifiable;
import pl.homeportal.commons.exception.HomeportalServiceException;

import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * Created by Grzegorz Wrażeń on 06-06-2020 at 09:22
 */

public class LoggingSupport
{
    // information
    public static final String INFORMATION_SAVE = "Entity %s saved successfully with data: %s";
    public static final String INFORMATION_UPDATE = "Entity %s updated successfully with data: %s";
    public static final String INFORMATION_DELETE = "Entity %s deleted successfully with id: %s";
    public static final String INFORMATION_INCREMENT_IDENTITY = "Increment identity for entity %s successful with data: %s";

    // warning
    public static final String WARNING_READ_NOT_FOUND = "Could not found entity %s with id: %s";
    public static final String WARNING_SAVE = "Could not save entity %s with data: %s";

    // error
    public static final String ERROR_SAVE = "Error during saving entity %s with data: %s";
    public static final String ERROR_UPDATE = "Error during updating entity %s with data: %s";
    public static final String ERROR_DELETE = "Error during deleting entity %s with id: %s";
    public static final String ERROR_INCREMENT_IDENTITY = "Error during incrementing identity for entity %s with data: %s";

    private static final String NULL_MESSAGE = "Method '%s' is trying to log null value.";
    private static final String METHOD_INFORMATION = "'information(...)'";
    private static final String METHOD_INFORMATION_SAVE_OR_UPDATE = "'informationSaveOrUpdate(...)'";
    private static final String METHOD_INFORMATION_DELETE = "'informationDelete(...)'";
    private static final String METHOD_WARNING_SAVE = "'warningSave(...)'";
    private static final String METHOD_WARNING_READ = "'warningRead(...)'";
    private static final String METHOD_ERROR_SAVE_OR_UPDATE = "'errorSaveOrUpdate(...)'";
    private static final String METHOD_ERROR_DELETE = "'errorDelete(...)'";

    private static final String NULL_ENTITY = "NULL ENTITY";

    // information
    public static void information(Logger logger, String message)
    {
        logger.info(message);
    }

    public static void information(Logger logger, String messageTemplate, Object... arguments)
    {
        String message = format(messageTemplate, arguments);
        logger.info(message);
    }

    public static <T extends Identifiable> void information(Logger logger, String messageTemplate, T entity)
    {
        if (Objects.isNull(entity))
        {
            warning(logger, NULL_MESSAGE, asList(METHOD_INFORMATION));
            return;
        }
        logger.info(formatMessage(messageTemplate, entity));
    }

    public static <T extends Identifiable> void informationSaveOrUpdate(Logger logger, T entity)
    {
        if (Objects.isNull(entity))
        {
            warning(logger, NULL_MESSAGE, asList(METHOD_INFORMATION_SAVE_OR_UPDATE));
            return;
        }

        if (entity.isTransient())
        {
            logger.info(formatMessage(INFORMATION_SAVE, entity));
            return;
        }
        logger.info(formatMessage(INFORMATION_UPDATE, entity));
    }

    public static <T extends Identifiable> void informationDelete(Logger logger, Class<T> aClass, Object id)
    {
        if (id == null)
        {
            warning(logger, NULL_MESSAGE, asList(METHOD_INFORMATION_DELETE));
            return;
        }
        logger.info(formatMessage(INFORMATION_DELETE, aClass, id));
    }

    // warning
//    public static void warning(Logger logger, String message)
//    {
//        logger.warn(message);
//    }

    public static void warning(Logger logger, String messageTemplate, Object... arguments)
    {
        String message = format(messageTemplate, arguments);
        logger.warn(message);
    }

    public static <T extends Identifiable> void warningSave(Logger logger, T entity)
    {
        if (entity == null)
        {
            warning(logger, NULL_MESSAGE, asList(METHOD_WARNING_SAVE));
            return;
        }
        logger.warn(formatMessage(WARNING_SAVE, entity));
    }

    public static <T extends Identifiable> void warningRead(Logger logger, Class<T> aClass, Object id)
    {
        if (id == null)
        {
            warning(logger, NULL_MESSAGE, asList(METHOD_WARNING_READ));
            return;
        }
        logger.warn(formatMessage(WARNING_READ_NOT_FOUND, aClass, id));
    }

    // error
    public static void error(Logger logger, String message)
    {
        logger.error(message);
    }

    public static void error(Logger logger, String message, Exception exception)
    {
        logger.error(message, exception);
    }

    public static void error(Logger logger, String messageTemplate, Object argument, Exception exception)
    {
        String message = format(messageTemplate, argument);
        logger.error(message, exception);
    }

    public static void error(Logger logger, Exception exception, String messageTemplate, Object... arguments)
    {
        final String message = format(messageTemplate, arguments);
        logger.error(message, exception);
    }

    public static void error(Logger logger, String messageTemplate, Object... arguments)
    {
        final String message = format(messageTemplate, arguments);
        logger.error(message);
    }

    public static <T extends Identifiable> String error(Logger logger, String messageTemplate, T entity)
    {
        String message = formatMessage(messageTemplate, entity);
        logger.error(message);
        return message;
    }

    public static <T extends Identifiable> String errorSaveOrUpdate(Logger logger, T entity)
    {
        if (entity == null)
        {
            warning(logger, NULL_MESSAGE, asList(METHOD_ERROR_SAVE_OR_UPDATE));
            return null;
        }

        if (entity.isTransient())
        {
            String message = formatMessage(ERROR_SAVE, entity);
            logger.error(message);
            return message;
        }
        String message = formatMessage(ERROR_UPDATE, entity);
        logger.error(message);
        return message;
    }

    public static <T extends Identifiable> String errorDelete(Logger logger, Class<T> aClass, Object id)
    {
        if (id == null)
        {
            warning(logger, NULL_MESSAGE, asList(METHOD_ERROR_DELETE));
            return null;
        }

        String message = formatMessage(ERROR_DELETE, aClass, id);
        logger.error(message);
        return message;
    }

    // log and serve exceptions
    /**
     * Przekazany wyjatek trafia teraz do logu — wczesniej parametr byl przyjmowany
     * i ignorowany, wiec przyczyna bledu ginela mimo poprawnego wywolania.
     */
    public static <T extends Identifiable> void logWithoutExceptionForSaveOrUpdate(Logger logger, T entity, Exception e)
    {
        if (entity == null)
        {
            warning(logger, NULL_MESSAGE, asList(METHOD_ERROR_SAVE_OR_UPDATE));
            return;
        }

        logger.error(formatMessage(entity.isTransient() ? ERROR_SAVE : ERROR_UPDATE, entity), e);
    }

    public static <T extends Identifiable> RuntimeException logWithExceptionForSaveOrUpdate(Logger logger, T entity, Exception e)
    {
        String message = errorSaveOrUpdate(logger, entity);
        return new HomeportalServiceException(message, e);
    }

    public static <T extends Identifiable> void logWithoutExceptionForDelete(Logger logger, Class<T> aClass, Object id, Exception e)
    {
        if (id == null)
        {
            warning(logger, NULL_MESSAGE, asList(METHOD_ERROR_DELETE));
            return;
        }

        logger.error(formatMessage(ERROR_DELETE, aClass, id), e);
    }

    public static <T extends Identifiable> RuntimeException logWithExceptionForDelete(Logger logger, Class<T> aClass, Object id, Exception e)
    {
        String message = errorDelete(logger, aClass, id);
        return new HomeportalServiceException(message, e);
    }

    public static <T extends Identifiable> RuntimeException logWithException(Logger logger, String messageTemplate, T entity, Exception e)
    {
        String message = formatMessage(messageTemplate, entity);
        logger.error(message, e);
        return new HomeportalServiceException(message, e);
    }

    public static <T extends Identifiable> void logWithoutException(Logger logger, String messageTemplate, T entity, Exception e)
    {
        String message = formatMessage(messageTemplate, entity);
        logger.error(message, e);
    }

    public static void logWithoutException(Logger logger, Exception e, String messageTemplate, Object... arguments)
    {
        final String message = format(messageTemplate, arguments);
        logger.error(message, e);
    }

    // generic message methods
    public static <T extends Identifiable> String formatMessage(String messageTemplate, T entity)
    {
        if (Objects.isNull(entity))
        {
            return format(messageTemplate, NULL_ENTITY, entity);
        }
        return format(messageTemplate, shortName(entity), entity);
    }

    public static <T extends Identifiable> String formatMessage(String messageTemplate, Class<T> aClass, Object id)
    {
        return format(messageTemplate, shortName(aClass), id);
    }

    public static <T extends Identifiable> String shortName(T entity)
    {
        return entity.getClass().getSimpleName();
    }

    public static <T> String shortName(Class<T> aClass)
    {
        return aClass.getSimpleName();
    }

    public static <T> Logger logger(Class<T> aClass)
    {
        return LoggerFactory.getLogger(shortName(aClass));
    }

    public static Logger logger(String loggerName)
    {
        return LoggerFactory.getLogger(loggerName);
    }
}
