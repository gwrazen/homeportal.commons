package pl.homeportal.commons.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.homeportal.commons.data.entity.AbstractEntity;
import pl.homeportal.commons.exception.HomeportalServiceException;

import javax.validation.constraints.NotNull;
import java.util.List;

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

    // information
    public static void information(Logger logger, String messageTemplate, List<Object> arguments)
    {
        logger.info(format(messageTemplate, arguments.toArray()));
    }

    public static <T extends AbstractEntity> void information(Logger logger, String messageTemplate, T entity)
    {
        if (entity == null)
        {
            warning(logger, NULL_MESSAGE, asList(METHOD_INFORMATION));
            return;
        }
        logger.info(formatMessage(messageTemplate, entity));
    }

    public static <T extends AbstractEntity> void informationSaveOrUpdate(Logger logger, T entity)
    {
        if (entity == null)
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

    public static <T extends AbstractEntity> void informationDelete(Logger logger, Class<T> aClass, Object id)
    {
        if (id == null)
        {
            warning(logger, NULL_MESSAGE, asList(METHOD_INFORMATION_DELETE));
            return;
        }
        logger.info(formatMessage(INFORMATION_DELETE, aClass, id));
    }

    // warning
    public static void warning(Logger logger, String messageTemplate, List<Object> arguments)
    {
        logger.warn(messageTemplate, arguments.toArray());
    }

    public static <T extends AbstractEntity> void warningSave(Logger logger, T entity)
    {
        if (entity == null)
        {
            warning(logger, NULL_MESSAGE, asList(METHOD_WARNING_SAVE));
            return;
        }
        logger.warn(formatMessage(WARNING_SAVE, entity));
    }

    public static <T extends AbstractEntity> void warningRead(Logger logger, Class<T> aClass, Object id)
    {
        if (id == null)
        {
            warning(logger, NULL_MESSAGE, asList(METHOD_WARNING_READ));
            return;
        }
        logger.warn(formatMessage(WARNING_READ_NOT_FOUND, aClass, id));
    }

    // error
    public static void error(Logger logger, String messageTemplate, List<Object> arguments)
    {
        String message = format(messageTemplate, arguments.toArray());
        logger.error(message);
    }

    public static <T extends AbstractEntity> String error(Logger logger, String messageTemplate, T entity)
    {
        String message = formatMessage(messageTemplate, entity);
        logger.error(message);
        return message;
    }

    public static <T extends AbstractEntity> String errorSaveOrUpdate(Logger logger, T entity)
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

    public static <T extends AbstractEntity> String errorDelete(Logger logger, Class<T> aClass, Object id)
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
    public static <T extends AbstractEntity> void logWithoutExceptionForSaveOrUpdate(Logger logger, T entity, Exception e)
    {
        errorSaveOrUpdate(logger, entity);
    }

    public static <T extends AbstractEntity> RuntimeException logWithExceptionForSaveOrUpdate(Logger logger, T entity, Exception e)
    {
        String message = errorSaveOrUpdate(logger, entity);
        return new HomeportalServiceException(message, e);
    }

    public static <T extends AbstractEntity> void logWithoutExceptionForDelete(Logger logger, Class<T> aClass, Object id, Exception e)
    {
        errorDelete(logger, aClass, id);
    }

    public static <T extends AbstractEntity> RuntimeException logWithExceptionForDelete(Logger logger, Class<T> aClass, Object id, Exception e)
    {
        String message = errorDelete(logger, aClass, id);
        return new HomeportalServiceException(message, e);
    }

    public static <T extends AbstractEntity> RuntimeException logWithException(Logger logger, String messageTemplate, T entity, Exception e)
    {
        String message = formatMessage(messageTemplate, entity);
        logger.error(message, e);
        return new HomeportalServiceException(message, e);
    }

    public static <T extends AbstractEntity> void logWithoutException(Logger logger, String messageTemplate, T entity, Exception e)
    {
        String message = formatMessage(messageTemplate, entity);
        logger.error(message, e);
    }

    public static void logWithoutException(Logger logger, String messageTemplate, List<Object> arguments, Exception e)
    {
        String message = format(messageTemplate, arguments.toArray());
        logger.error(message, e);
    }

    // generic message methods
    public static <T extends AbstractEntity> String formatMessage(String messageTemplate, T entity)
    {
        return format(messageTemplate, shortName(entity), entity);
    }

    public static <T extends AbstractEntity> String formatMessage(String messageTemplate, Class<T> aClass, Object id)
    {
        return format(messageTemplate, shortName(aClass), id);
    }

    public static <T extends AbstractEntity> String shortName(T entity)
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
}
