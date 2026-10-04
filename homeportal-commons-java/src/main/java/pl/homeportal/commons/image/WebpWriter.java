package pl.homeportal.commons.image;

import com.luciad.imageio.webp.WebPWriteParam;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

/**
 * Lossy WebP next to the JPG/PNG variants of an offer photo; native libwebp ships inside webp-imageio.
 */
public final class WebpWriter
{
    public static final String EXTENSION = "webp";

    // q80 saved only 9 % on agency JPGs, q70 saves 25 % with no visible loss (measured 2026-10-04)
    static final float QUALITY = 0.70f;

    private static final String MIME_TYPE = "image/webp";

    private WebpWriter()
    {
    }

    /** {@code abc_m.jpg} -> {@code abc_m.webp} in the same directory. */
    public static File sibling(File file)
    {
        final String name = file.getName();
        final int dot = name.lastIndexOf('.');
        final String base = dot > 0 ? name.substring(0, dot) : name;
        return new File(file.getParentFile(), base + "." + EXTENSION);
    }

    /** Writes through a temp file and an atomic move, so a concurrent reader never sees a half-written file. */
    public static void write(BufferedImage image, File target) throws IOException
    {
        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(MIME_TYPE);
        if (!writers.hasNext())
        {
            throw new IOException("No WebP image writer on the classpath");
        }

        final ImageWriter writer = writers.next();
        final File temp = File.createTempFile(target.getName(), ".tmp", target.getParentFile());
        try
        {
            final WebPWriteParam param = new WebPWriteParam(writer.getLocale());
            param.setCompressionMode(WebPWriteParam.MODE_EXPLICIT);
            param.setCompressionType(param.getCompressionTypes()[WebPWriteParam.LOSSY_COMPRESSION]);
            param.setCompressionQuality(QUALITY);

            try (FileImageOutputStream output = new FileImageOutputStream(temp))
            {
                writer.setOutput(output);
                writer.write(null, new IIOImage(toEncodable(image), null, null), param);
            }
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        finally
        {
            writer.dispose();
            Files.deleteIfExists(temp.toPath());
        }
    }

    // grey and indexed images are redrawn as RGB(A) before they reach the native encoder
    private static BufferedImage toEncodable(BufferedImage image)
    {
        switch (image.getType())
        {
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_4BYTE_ABGR:
                return image;
            default:
                final int type = image.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
                final BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), type);
                final Graphics2D g = copy.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                return copy;
        }
    }
}
