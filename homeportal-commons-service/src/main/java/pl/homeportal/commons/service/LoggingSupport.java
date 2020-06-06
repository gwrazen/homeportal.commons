package pl.homeportal.commons.service;

import org.slf4j.Logger;
import pl.homeportal.commons.data.entity.AbstractEntity;
import pl.homeportal.commons.exception.HomeportalServiceException;

import javax.validation.constraints.NotNull;

/**
 * Created by Grzegorz Wrażeń on 06-06-2020 at 09:22
 */

public class LoggingSupport
{
    // information
    public static final String INFORMATION_SAVE = "Entity s% saved successfully with data: %s";
    public static final String INFORMATION_UPDATE = "Entity s% updated successfully with data: %s";
    public static final String INFORMATION_INCREMENT_IDENTITY = "Increment identity for entity %s successful with data: %s";

    // warning
    public static final String WARNING_READ_NOT_FOUND = "Could not found entity %s with id: %s";
    public static final String WARNING_SAVE = "Could not save entity %s with data: %s";

    // error
    public static final String ERROR_SAVE = "Error during saving entity %s with data: %s";
    public static final String ERROR_UPDATE = "Error during updating entity %s with data: %s";
    public static final String ERROR_INCREMENT_IDENTITY = "Error during incrementing identity for entity %s with data: %s";

    // information
    public static <T extends AbstractEntity> String informationSave(T entity)
    {
        if (entity.isTransient())
        {
            return formatMessage(INFORMATION_SAVE, entity);
        }
        return formatMessage(INFORMATION_UPDATE, entity);
    }

    // warning
    public static <T extends AbstractEntity> String warningSave(T entity)
    {
        return formatMessage(WARNING_SAVE, entity);
    }

    public static <T extends AbstractEntity> String warningRead(Class<T> aClass, Object id)
    {
        return formatMessage(WARNING_READ_NOT_FOUND, aClass, id);
    }

    // error
    public static <T extends AbstractEntity> String errorSaveOrUpdate(T entity)
    {
        if (entity.isTransient())
        {
            return formatMessage(ERROR_SAVE, entity);
        }
        return formatMessage(ERROR_UPDATE, entity);
    }

    // log and serve exceptions
    public static <T extends AbstractEntity> void logWithoutExceptionForSave(Logger logger, T entity, Exception e)
    {
        String message = errorSaveOrUpdate(entity);
        logger.error(message, e);
    }

    public static <T extends AbstractEntity> RuntimeException logWithExceptionForSave(Logger logger, T entity, Exception e)
    {
        String message = errorSaveOrUpdate(entity);
        logger.error(message, e);
        return new HomeportalServiceException(message, e);
    }

    public static <T extends AbstractEntity> RuntimeException logWithException(String messageTemplate, Logger logger, T entity, Exception e)
    {
        String message = formatMessage(messageTemplate, entity);
        logger.error(message, e);
        return new HomeportalServiceException(message, e);
    }

    public static <T extends AbstractEntity> void logWithoutException(String messageTemplate, Logger logger, T entity, Exception e)
    {
        String message = formatMessage(messageTemplate, entity);
        logger.error(message, e);
    }

    // generic message methods
    public static <T extends AbstractEntity> String formatMessage(String messageTemplate, T entity)
    {
        return String.format(messageTemplate, shortName(entity), entity);
    }

    public static <T extends AbstractEntity> String formatMessage(String messageTemplate, Class<T> aClass, Object id)
    {
        return String.format(messageTemplate, shortName(aClass), id);
    }

    public static <T extends AbstractEntity> String shortName(@NotNull T entity)
    {
        return entity.getClass().getSimpleName();
    }

    public static <T> String shortName(@NotNull Class<T> aClass)
    {
        return aClass.getSimpleName();
    }
}
