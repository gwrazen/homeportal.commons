package pl.homeportal.commons.file;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilesTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void deletesMatchingFiles() throws Exception
    {
        // given
        folder.newFile("oferty_202608_1.zip");
        final File keep = folder.newFile("oferty.xml");

        // when
        Files.deleteFiles(folder.getRoot(), Pattern.compile("oferty_\\d*_\\d*.zip"));

        // then
        assertFalse(new File(folder.getRoot(), "oferty_202608_1.zip").exists());
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
        final File nested = folder.newFolder("oferty_202608_1.zip");
        final File inside = new File(nested, "oferty_202608_2.zip");
        assertTrue(inside.createNewFile());

        // when
        Files.deleteFiles(folder.getRoot(), Pattern.compile("oferty_\\d*_\\d*.zip"));

        // then
        assertFalse(inside.exists());
    }

    @Test
    public void deleteDirectoryRemovesTreeAndReportsResult() throws Exception
    {
        // given
        final File nested = folder.newFolder("agency", "photos");
        assertTrue(new File(nested, "photo_s.jpg").createNewFile());

        // when
        final boolean deleted = Files.deleteDirectory(new File(folder.getRoot(), "agency"));

        // then
        assertTrue(deleted);
        assertFalse(new File(folder.getRoot(), "agency").exists());
    }

    @Test
    public void deleteDirectoryIsNullSafe()
    {
        assertFalse(Files.deleteDirectory(null));
    }
}
