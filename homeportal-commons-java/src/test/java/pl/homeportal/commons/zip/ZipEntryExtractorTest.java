package pl.homeportal.commons.zip;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import pl.homeportal.commons.exception.HomeportalServiceException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ZipEntryExtractorTest
{
    private static final String ENTRY = "oferty.xml";
    private static final String CONTENT = "<?xml version=\"1.0\"?><offers/>";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void extractsEntryContent() throws Exception
    {
        // given
        final File archive = archiveWith(ENTRY, CONTENT);

        // when
        try (InputStream stream = ZipEntryExtractor.extract(ENTRY, archive.getAbsolutePath()))
        {
            // then
            assertEquals(CONTENT, read(stream));
        }
    }

    /**
     * Regresja: stary kod zwracal archive.getInputStream(entry) bez zamkniecia ZipFile.
     * Zamkniecie zwroconego strumienia nie zwalnia archiwum, wiec importer wyczerpywal
     * deskryptory w petli po paczkach, a na Windowsie plik zostawal zablokowany.
     */
    @Test
    public void releasesArchiveAfterExtract() throws Exception
    {
        // given
        final File archive = archiveWith(ENTRY, CONTENT);

        // when
        ZipEntryExtractor.extract(ENTRY, archive.getAbsolutePath());

        // then — archiwum nie jest juz trzymane, wiec da sie je usunac
        assertTrue(archive.delete());
    }

    @Test(expected = HomeportalServiceException.class)
    public void throwsWhenEntryIsMissing() throws Exception
    {
        // given
        final File archive = archiveWith(ENTRY, CONTENT);

        // when
        ZipEntryExtractor.extract("nieistniejacy.xml", archive.getAbsolutePath());
    }

    @Test
    public void isAvailableForRealArchive() throws Exception
    {
        assertTrue(ZipEntryExtractor.isAvailable(archiveWith(ENTRY, CONTENT).getAbsolutePath()));
    }

    /**
     * Regresja: ZipInputStream.available() zwraca 1 dla dowolnego strumienia, ktory
     * nie jest na koncu — nigdy nie zaglada w naglowek ZIP-a. Kazdy czytelny plik
     * przechodzil wiec bramke isAvailable, a blad wychodzil dopiero przy odczycie.
     */
    @Test
    public void isNotAvailableForPlainTextFile() throws Exception
    {
        // given
        final File notAnArchive = folder.newFile("notes.txt");
        try (FileOutputStream out = new FileOutputStream(notAnArchive))
        {
            out.write("to nie jest zip".getBytes(StandardCharsets.UTF_8));
        }

        // then
        assertFalse(ZipEntryExtractor.isAvailable(notAnArchive.getAbsolutePath()));
    }

    @Test
    public void isNotAvailableForMissingFile()
    {
        assertFalse(ZipEntryExtractor.isAvailable(new File(folder.getRoot(), "brak.zip").getAbsolutePath()));
    }

    private File archiveWith(String entryName, String content) throws IOException
    {
        final File archive = folder.newFile(entryName.hashCode() + "-archive.zip");
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(archive)))
        {
            zip.putNextEntry(new ZipEntry(entryName));
            zip.write(content.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        return archive;
    }

    private String read(InputStream stream) throws IOException
    {
        final java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        final byte[] chunk = new byte[1024];
        int read;
        while ((read = stream.read(chunk)) != -1)
        {
            buffer.write(chunk, 0, read);
        }

        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }
}
