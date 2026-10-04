package pl.homeportal.commons.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageProcessorTest
{
    @TempDir
    public File folder;

    @Test
    public void writesWebpNextToEveryJpgVariant() throws Exception
    {
        // given
        final ImageProcessor processor = new ImageProcessor();
        processor.add("Photo.JPG", encode(photo(1200, 900, BufferedImage.TYPE_INT_RGB), "jpg"), folder);

        // when
        processor.start();
        processor.join();

        // then
        assertDecodes("photo_s.jpg", 320, 240);
        assertDecodes("photo_s.webp", 320, 240);
        assertDecodes("photo_m.jpg", 600, 450);
        assertDecodes("photo_m.webp", 600, 450);
        assertDecodes("photo_l.jpg", 1024, 768);
        assertDecodes("photo_l.webp", 1024, 768);
    }

    @Test
    public void writesWebpForGreyPng() throws Exception
    {
        // given
        final ImageProcessor processor = new ImageProcessor();
        processor.add("scan.png", encode(photo(800, 600, BufferedImage.TYPE_BYTE_GRAY), "png"), folder);

        // when
        processor.start();
        processor.join();

        // then
        assertDecodes("scan_m.png", 600, 450);
        assertDecodes("scan_m.webp", 600, 450);
    }

    @Test
    public void siblingSwapsOnlyTheExtension()
    {
        assertEquals(new File("/a/b/x_m.webp"), WebpWriter.sibling(new File("/a/b/x_m.jpg")));
        assertEquals(new File("/a/b/x.y_l.webp"), WebpWriter.sibling(new File("/a/b/x.y_l.jpeg")));
    }

    @Test
    public void writeLeavesNoTempFileBehind() throws Exception
    {
        // given
        final File target = new File(folder, "x_m.webp");

        // when
        WebpWriter.write(photo(600, 450, BufferedImage.TYPE_INT_RGB), target);

        // then
        assertTrue(target.length() > 0);
        final String[] names = folder.list();
        assertNotNull(names);
        assertEquals(1, names.length);
        assertFalse(names[0].endsWith(".tmp"));
    }

    private void assertDecodes(String name, int width, int height) throws Exception
    {
        final BufferedImage image = ImageIO.read(new File(folder, name));
        assertNotNull(image, name);
        assertEquals(width, image.getWidth(), name);
        assertEquals(height, image.getHeight(), name);
    }

    private static BufferedImage photo(int width, int height, int type)
    {
        final BufferedImage image = new BufferedImage(width, height, type);
        final Graphics2D g = image.createGraphics();
        g.setColor(Color.ORANGE);
        g.fillRect(0, 0, width / 2, height);
        g.setColor(Color.BLUE);
        g.fillOval(width / 3, height / 4, width / 3, height / 2);
        g.dispose();
        return image;
    }

    private static byte[] encode(BufferedImage image, String format) throws Exception
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return out.toByteArray();
    }
}
