package pl.homeportal.commons.image;

import pl.homeportal.commons.exception.HomeportalServiceException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author gwrazen
 */
public class ImageResizer
{
    public static void resizeUnproportionally(BufferedImage originalImage, File destinationImage, int width, int height, String extension) throws Exception
    {
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        if (!ImageIO.write(resizedImage, extension, destinationImage))
        {
            throw new HomeportalServiceException("No image writer for extension: '" + extension + "'");
        }
    }
}
