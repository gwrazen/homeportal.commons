package pl.homeportal.commons.zip;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static pl.homeportal.commons.text.Constants.UTF_8;

/**
 * Created by Grzegorz Wrażeń on 21-09-2013 at 14:53
 */

public class ZipEntryExtractor
{
    private static final Set<String> TEXT_SUFFIXES = new HashSet();

    static
    {
        TEXT_SUFFIXES.add(".txt");
        TEXT_SUFFIXES.add(".xml");
        TEXT_SUFFIXES.add(".json");
    }

    public static InputStream extract(String name, String archivePath) throws Exception
    {
        ZipFile archive = new ZipFile(archivePath);
        ZipEntry entry = archive.getEntry(name);

        return archive.getInputStream(entry);
    }

    public static boolean isAvailable(String archivePath)
    {
        try
        {
            return (new ZipInputStream(new FileInputStream(archivePath), Charset.forName(UTF_8)).available() == 1 ? true : false);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private static boolean isTextFile(String name)
    {
        for (String suffix : TEXT_SUFFIXES)
        {
            if (name.endsWith(suffix))
            {
                return true;
            }
        }

        return false;
    }
}
