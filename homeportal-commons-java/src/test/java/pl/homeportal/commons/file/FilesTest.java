package pl.homeportal.commons.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;

public class FilesTest
{
    @TempDir
    public File folder;

    @Test
    public void deletesMatchingFiles() throws Exception
    {
        // given
        newFile(folder, "oferty_202608_1.zip");
        final File keep = newFile(folder, "oferty.xml");

        // when
        Files.deleteFiles(folder, Pattern.compile("oferty_\\d*_\\d*.zip"));

        // then
        assertFalse(new File(folder, "oferty_202608_1.zip").exists());
        assertTrue(keep.exists());
    }

    /**
     * Regresja: wzorzec byl sprawdzany takze dla katalogow, a pasujacy katalog
     * konczyl sie nieudanym delete() i pominieciem rekurencji przez `continue` —
     * pliki w srodku nigdy nie byly odwiedzane, a log i tak mowil "Deleted file".
     */
    @Test
    public void recursesIntoDirectoryMatchingThePattern() throws Exception
    {
        // given
        final File nested = newFolder(folder, "oferty_202608_1.zip");
        final File inside = new File(nested, "oferty_202608_2.zip");
        assertTrue(inside.createNewFile());

        // when
        Files.deleteFiles(folder, Pattern.compile("oferty_\\d*_\\d*.zip"));

        // then
        assertFalse(inside.exists());
    }

    @Test
    public void deleteDirectoryRemovesTreeAndReportsResult() throws Exception
    {
        // given
        final File nested = newFolder(folder, "agency", "photos");
        assertTrue(new File(nested, "photo_s.jpg").createNewFile());

        // when
        final boolean deleted = Files.deleteDirectory(new File(folder, "agency"));

        // then
        assertTrue(deleted);
        assertFalse(new File(folder, "agency").exists());
    }

    @Test
    public void deleteDirectoryIsNullSafe()
    {
        assertFalse(Files.deleteDirectory(null));
    }

    /** Odpowiednik TemporaryFolder.newFile - @TempDir daje sam katalog, bez API do tworzenia. */
    private static File newFile(final File root, final String name) throws IOException
    {
        final File file = new File(root, name);
        file.createNewFile();
        return file;
    }

    /** Odpowiednik TemporaryFolder.newFolder - sklada sciezke z czlonow i tworzy ja w calosci. */
    private static File newFolder(final File root, final String... path)
    {
        final File directory = new File(root, String.join(File.separator, path));
        directory.mkdirs();
        return directory;
    }
}
