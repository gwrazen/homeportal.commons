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
                        file.delete();
                        log.info(String.format(DELETED_FILE, file.getName()));
                        continue;
                    }
                    deleteDirectory(file);
                }
            }
            return directory.delete();
        }
        catch (Exception e)
        {
            log.error(String.format(ERROR_DELETING_FILE, directory.getAbsolutePath()));
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
                    if (filePattern.matcher(file.getName()).matches())
                    {
                        file.delete();
                        log.info(String.format(DELETED_FILE, file.getName()));
                        continue;
                    }
                    if (file.isDirectory())
                    {
                        deleteFiles(file, filePattern);
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
