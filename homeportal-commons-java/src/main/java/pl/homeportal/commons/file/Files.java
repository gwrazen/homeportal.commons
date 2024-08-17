package pl.homeportal.commons.file;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by Grzegorz Wrażeń on 16-08-2024 at 16:49
 */

public class Files
{
    public static boolean deleteDirectory(File directory)
    {
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
                        continue;
                    }
                    deleteDirectory(file);
                }
            }
            return directory.delete();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static void deleteFiles(File directory, Pattern filePattern)
    {
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
                        continue;
                    }
                    deleteDirectory(file);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
