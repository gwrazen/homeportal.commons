package pl.homeportal.commons.zip;

import pl.homeportal.commons.exception.HomeportalServiceException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.lang.String.format;

/**
 * Created by Grzegorz Wrażeń on 21-09-2013 at 14:53
 */

public class ZipEntryExtractor
{
    private static final String ENTRY_NOT_FOUND = "Entry '%s' not found in archive: %s";
    private static final int BUFFER_SIZE = 8192;

    /**
     * Wpis jest wczytywany w calosci w obrebie try-with-resources: zamkniecie
     * zwroconego strumienia NIE zwalnia uchwytu do ZipFile, wiec zwracanie
     * archive.getInputStream(entry) wyciekalo deskryptorem przy kazdym wywolaniu.
     */
    public static InputStream extract(String name, String archivePath) throws Exception
    {
        try (ZipFile archive = new ZipFile(archivePath))
        {
            ZipEntry entry = archive.getEntry(name);
            if (entry == null)
            {
                throw new HomeportalServiceException(format(ENTRY_NOT_FOUND, name, archivePath));
            }

            try (InputStream entryStream = archive.getInputStream(entry))
            {
                return new ByteArrayInputStream(readFully(entryStream));
            }
        }
    }

    /**
     * ZipInputStream.available() zwraca 1 dla dowolnego strumienia, ktory nie jest
     * na koncu — nigdy nie zaglada w naglowek ZIP-a. Jedyny wiarygodny test to
     * proba otwarcia archiwum.
     */
    public static boolean isAvailable(String archivePath)
    {
        try (ZipFile archive = new ZipFile(archivePath))
        {
            return archive.entries().hasMoreElements();
        }
        catch (IOException e)
        {
            return false;
        }
    }

    private static byte[] readFully(InputStream source) throws IOException
    {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[BUFFER_SIZE];
        int read;
        while ((read = source.read(chunk)) != -1)
        {
            buffer.write(chunk, 0, read);
        }

        return buffer.toByteArray();
    }
}
