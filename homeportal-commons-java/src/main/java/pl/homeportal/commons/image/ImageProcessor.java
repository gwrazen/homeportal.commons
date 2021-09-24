package pl.homeportal.commons.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessor extends Thread
{
    private List<ImageProcessorTask> tasks = new ArrayList<>();

    public ImageProcessor()
    {
        super();
    }

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
        for (ImageProcessorTask task : tasks)
        {
            new Thread(task).start();
        }

        int finished = 0;
        while (finished < tasks.size())
        {
            try
            {
                Thread.sleep(1);
            }
            catch (Exception e)
            {
            }

            finished = 0;
            for (ImageProcessorTask task : tasks)
            {
                if (task.isFinished()) {
                    ++finished;
                }
            }
        }
    }
}

class ImageProcessorTask implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger(ImageProcessorTask.class.getSimpleName());

    private static final int SMALL_WIDTH = 320;
    private static final int SMALL_HEIGHT = 240;

    private static final int MEDIUM_WIDTH = 600;
    private static final int MEDIUM_HEIGHT = 450;

    private static final int LARGE_WIDTH = 1024;
    private static final int LARGE_HEIGHT = 768;

    private static final String JPG = "jpg";
    private static final String SMALL = "_s";
    private static final String MEDIUM = "_m";
    private static final String LARGE = "_l";
    private static final String DOT = ".";
    private static final String SLASH = "/";


    private InputStream sourceFile;
    private File destinationDir;
    private String name;
    private String extension;

    boolean finished = false;

    public ImageProcessorTask(String fileName, InputStream sourceFile, File destinationDir)
    {
        this.sourceFile = sourceFile;
        this.destinationDir = destinationDir;
        this.name = getName(fileName);
        this.extension = getExtension(fileName);
    }

    @Override
    public void run()
    {
        try
        {
            LOG.info("Processing image file name: " + name + DOT + extension);
            BufferedImage originalImage = ImageIO.read(sourceFile);
            String smallLink = destinationDir.getAbsolutePath() + SLASH + name + SMALL + DOT + extension;
            File small = new File(smallLink);
            ImageResizer.resizeUnproportionally(originalImage, small, SMALL_WIDTH, SMALL_HEIGHT, extension);
            resizeImage(originalImage, MEDIUM, MEDIUM_WIDTH, MEDIUM_HEIGHT);
            resizeImage(originalImage, LARGE, LARGE_WIDTH, LARGE_HEIGHT);
            sourceFile.close();
        }
        catch (Exception e)
        {
            LOG.error("IMAGE file processing ERROR", e);
        }

        finished = true;
    }

    public boolean isFinished()
    {
        return finished;
    }

    private void resizeImage(BufferedImage originalImage, String medium2, int mediumWidth, int mediumHeight) throws IOException
    {
        String mediumLink = destinationDir.getAbsolutePath() + SLASH + name + medium2 + DOT + JPG;
        File medium = new File(mediumLink);
        BufferedImage mediumImage = Scalr.resize(originalImage, Scalr.Method.SPEED, mediumWidth, mediumHeight);
        ImageIO.write(mediumImage, JPG, medium);
        mediumImage.getGraphics().dispose();
    }

    private String getName(String name)
    {
        return name.substring(0, name.lastIndexOf(DOT)).toLowerCase();
    }

    private String getExtension(String name)
    {
        return name.substring(name.lastIndexOf(DOT) + 1, name.length());
    }
}
