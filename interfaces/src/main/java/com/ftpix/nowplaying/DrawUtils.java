package com.ftpix.nowplaying;

import net.coobird.thumbnailator.Thumbnails;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class DrawUtils {

    public static void drawString(String text, Graphics2D graphics, Rectangle r) {
        Font font;
        int fontSize = 0;
        Rectangle2D stringBounds;
        do {
            fontSize++;
            font = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);
            stringBounds = font.getStringBounds(text, graphics.getFontRenderContext());
        } while (r.getWidth() > stringBounds.getWidth() && r.getHeight() > stringBounds.getHeight());

        font = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize - 1);

        int y = (int) (r.y + (r.getHeight()) - (stringBounds.getHeight() / 4));
        graphics.setFont(font);
        graphics.drawString(text, r.x, y);
    }

    public static void drawImage(BufferedImage img, Graphics2D graphics, Rectangle area, boolean roundedBorders) throws IOException {
        if (img != null) {
            BufferedImage scaledThumb = Thumbnails.of(img).size(area.width, area.height).asBufferedImage();
            if (roundedBorders) {
                scaledThumb = Utils.makeRoundedCorner(scaledThumb, 20);
            }
            graphics.drawImage(scaledThumb, null, area.x + (area.width / 2) - (scaledThumb.getWidth() / 2), area.y + (area.height / 2) - (scaledThumb.getHeight()) / 2);
        }
    }
}
