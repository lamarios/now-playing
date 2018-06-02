package com.ftpix.nowplaying;

import net.coobird.thumbnailator.Thumbnails;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ForkJoinPool;

public class Utils {

    public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)

        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);

        g2.dispose();

        return output;
    }

    /**
     * Try to fit string in given width
     *
     * @param text
     * @param wantedFontSize
     * @param maxLength
     * @param graphics
     * @param x
     * @param y
     * @return
     */
    public static void fitString(String text, int wantedFontSize, int maxLength, Graphics2D graphics, int x, int y) {
        Font font;
        int textWidth = 0;
        do {
            wantedFontSize--;
            font = new Font(Font.SANS_SERIF, Font.PLAIN, wantedFontSize);
            textWidth = (int) font.getStringBounds(text, graphics.getFontRenderContext()).getWidth();
        } while (textWidth > maxLength);

        graphics.setFont(font);
        graphics.drawString(text, x, y);
    }


    /**
     * GEts a x percent of a dimension
     *
     * @param dimension
     * @param percent
     * @return
     */
    public static Dimension getPercentOf(Dimension dimension, int percent) {
        double width = (dimension.getWidth() / 100d) * percent;
        double height = (dimension.getHeight() / 100d) * percent;

        return new Dimension((int) width, (int) height);
    }

    /**
     * Resize an image to cover the given dimension
     *
     * @param image the image to resize
     * @param dimension the dimension to cover
     * @return the image resized to covert
     */
    public static BufferedImage cover(BufferedImage image, Dimension dimension) throws IOException {

        Thumbnails.Builder<BufferedImage> thumb = Thumbnails.of(image);
        double artRatio = (double) image.getWidth() / (double) image.getHeight();
        double screenRatio = (double) dimension.width / (double) dimension.height;
        if (artRatio > screenRatio) {
            thumb = thumb.height(dimension.height);
        } else {
            thumb = thumb.width(dimension.width);
        }

        return thumb.asBufferedImage();
    }
}
