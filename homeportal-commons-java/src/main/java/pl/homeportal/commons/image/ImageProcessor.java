package pl.homeportal.commons.image;

import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.homeportal.commons.exception.HomeportalServiceException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;

/**
 * Nadal jest watkiem (konsumenci wolaja start()/join()), ale zadania wykonuje pula
 * o ograniczonym rozmiarze zamiast jednego watku OS na obrazek. Poprzednia wersja
 * czekala w petli na nie-volatile fladze `finished` z pustym catch — bez bariery
 * pamieci petla mogla nie zakonczyc sie nigdy i nie dala sie przerwac.
 */
public class ImageProcessor extends Thread
{
    private static final Logger LOG = LoggerFactory.getLogger(ImageProcessor.class);
    private static final int MAX_THREADS = 4;

    private final List<ImageProcessorTask> tasks = new ArrayList<>();

    public void add(String fileName, InputStream sourceFile, File destinationDir)
    {
        tasks.add(new ImageProcessorTask(fileName, sourceFile, destinationDir));
    }

    public void add(String fileName, byte[] content, File destinationDir)
    {
        tasks.add(new ImageProcessorTask(fileName, new ByteArrayInputStream(content), destinationDir));
    }

    @Override
    public void run()
    {
        if (tasks.isEmpty())
        {
            return;
        }

        final int threads = Math.min(tasks.size(), Math.min(MAX_THREADS, Runtime.getRuntime().availableProcessors()));
        final ExecutorService executor = Executors.newFixedThreadPool(threads);
        try
        {
            executor.invokeAll(new ArrayList<Callable<Void>>(tasks));
        }
        catch (InterruptedException e)
        {
            // Przywrocenie flagi: bez tego sygnal zamkniecia kontekstu ginal.
            Thread.currentThread().interrupt();
            LOG.warn("Image processing interrupted");
        }
        finally
        {
            executor.shutdownNow();
        }
    }
}

class ImageProcessorTask implements Callable<Void>
{
    private static final Logger LOG = LoggerFactory.getLogger(ImageProcessorTask.class);

    private static final int SMALL_WIDTH = 320;
    private static final int SMALL_HEIGHT = 240;

    private static final int MEDIUM_WIDTH = 600;
    private static final int MEDIUM_HEIGHT = 450;

    private static final int LARGE_WIDTH = 1024;
    private static final int LARGE_HEIGHT = 768;

    private static final String SMALL = "_s";
    private static final String MEDIUM = "_m";
    private static final String LARGE = "_l";
    private static final String DOT = ".";
    private static final String SLASH = "/";

    private static final String NO_EXTENSION = "Image file name without an extension: '%s'";

    private final InputStream sourceFile;
    private final File destinationDir;
    private final String name;
    private final String extension;

    ImageProcessorTask(String fileName, InputStream sourceFile, File destinationDir)
    {
        this.sourceFile = sourceFile;
        this.destinationDir = destinationDir;
        this.name = getName(fileName);
        this.extension = getExtension(fileName);
    }

    @Override
    public Void call()
    {
        // try-with-resources: przy bledzie dekodowania stary kod nie dochodzil do
        // sourceFile.close(), wiec kazdy uszkodzony upload zostawial otwarty uchwyt.
        try (InputStream source = sourceFile)
        {
            LOG.info("Processing image file name: {}{}{}", name, DOT, extension);
            BufferedImage originalImage = ImageIO.read(source);
            if (originalImage == null)
            {
                throw new HomeportalServiceException(format("Unsupported image format: '%s'", extension));
            }

            String smallLink = destinationDir.getAbsolutePath() + SLASH + name + SMALL + DOT + extension;
            ImageResizer.resizeUnproportionally(originalImage, new File(smallLink), SMALL_WIDTH, SMALL_HEIGHT, extension);
            resizeImage(originalImage, MEDIUM, MEDIUM_WIDTH, MEDIUM_HEIGHT, extension);
            resizeImage(originalImage, LARGE, LARGE_WIDTH, LARGE_HEIGHT, extension);
        }
        catch (Exception e)
        {
            LOG.error("IMAGE file processing ERROR", e);
        }

        return null;
    }

    private void resizeImage(BufferedImage originalImage, String sizeSuffix, int width, int height, String extension) throws IOException
    {
        String link = destinationDir.getAbsolutePath() + SLASH + name + sizeSuffix + DOT + extension;
        File file = new File(link);
        BufferedImage mediumImage = Scalr.resize(originalImage, Scalr.Method.SPEED, width, height);
        // ImageIO.write zwraca false zamiast rzucac, gdy dla formatu nie ma writera —
        // efektem byl "sukces" bez zapisanego pliku i 404 na miniaturze.
        if (!ImageIO.write(mediumImage, extension, file))
        {
            throw new HomeportalServiceException(format("No image writer for extension: '%s'", extension));
        }
        mediumImage.getGraphics().dispose();
    }

    private String getName(String fileName)
    {
        return fileName.substring(0, dotIndex(fileName)).toLowerCase();
    }

    private String getExtension(String fileName)
    {
        return fileName.substring(dotIndex(fileName) + 1);
    }

    private int dotIndex(String fileName)
    {
        // substring(0, -1) rzucalo StringIndexOutOfBounds na watku wolajacego,
        // czyli poza catch-em z run() — z komunikatem nic nie mowiacym o przyczynie.
        final int index = fileName == null ? -1 : fileName.lastIndexOf(DOT);
        if (index < 1 || index == fileName.length() - 1)
        {
            throw new HomeportalServiceException(format(NO_EXTENSION, fileName));
        }

        return index;
    }
}
