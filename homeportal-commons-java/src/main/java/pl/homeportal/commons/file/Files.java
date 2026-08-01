package pl.homeportal.commons.file;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by Grzegorz Wrażeń on 16-08-2024 at 16:49
 */

@Slf4j
public class Files
{
    public static final String ERROR_DELETING_FILE = "Error deleting file: %s";
    public static final String DELETED_FILE = "Deleted file: %s";
    public static final String NOT_DELETED_FILE = "Could not delete file: %s";

    /**
     * File.delete() zwraca boolean i wczesniej byl ignorowany — log mowil
     * "Deleted file", nawet gdy plik zostawal na dysku (read-only, blokada).
     */
    private static boolean delete(File file)
    {
        final boolean deleted = file.delete();
        if (deleted)
        {
            log.info(String.format(DELETED_FILE, file.getName()));
        }
        else
        {
            log.warn(String.format(NOT_DELETED_FILE, file.getAbsolutePath()));
        }

        return deleted;
    }

    public static boolean deleteDirectory(File directory)
    {
        if (directory == null)
        {
            return false;
        }

        try
        {
            final File[] files = directory.listFiles();
            if (files != null)
            {
                for (File file : files)
                {
                    if (file.isFile())
                    {
                        delete(file);
                        continue;
                    }
                    deleteDirectory(file);
                }
            }
            return directory.delete();
        }
        catch (Exception e)
        {
            log.error(String.format(ERROR_DELETING_FILE, directory.getAbsolutePath()), e);
            return false;
        }
    }

    public static void deleteFiles(File directory, Pattern filePattern)
    {
        if (directory == null || filePattern == null)
        {
            return;
        }

        try
        {
            final File[] files = directory.listFiles();
            if (files != null)
            {
                for (File file : files)
                {
                    // Katalog sprawdzamy PRZED wzorcem: wczesniej katalog pasujacy
                    // do wzorca konczyl sie nieudanym delete() i pominieciem rekurencji,
                    // wiec pliki w srodku nigdy nie byly odwiedzane.
                    if (file.isDirectory())
                    {
                        deleteFiles(file, filePattern);
                        continue;
                    }
                    if (filePattern.matcher(file.getName()).matches())
                    {
                        delete(file);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(String.format(ERROR_DELETING_FILE, directory.getAbsolutePath()), e);
        }
    }
}
