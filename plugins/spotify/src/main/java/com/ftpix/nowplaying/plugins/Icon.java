package com.ftpix.nowplaying.plugins;

import java.awt.*;
import java.awt.geom.*;
import static java.awt.Color.*;
import static java.awt.MultipleGradientPaint.CycleMethod.*;
import static java.awt.MultipleGradientPaint.ColorSpaceType.*;

/**
 * This class has been automatically generated using
 * <a href="http://ebourg.github.io/flamingo-svg-transcoder/">Flamingo SVG transcoder</a>.
 */
public class Icon {

    /**
     * Paints the transcoded SVG image on the specified graphics context. You
     * can install a custom transformation on the graphics context to scale the
     * image.
     * 
     * @param g Graphics context.
     */
    public static void paint(Graphics2D g) {
        Shape shape = null;
        
        float origAlpha = 1.0f;
        Composite origComposite = g.getComposite();
        if (origComposite instanceof AlphaComposite) {
            AlphaComposite origAlphaComposite = (AlphaComposite)origComposite;
            if (origAlphaComposite.getRule() == AlphaComposite.SRC_OVER) {
                origAlpha = origAlphaComposite.getAlpha();
            }
        }
        
        java.util.LinkedList<AffineTransform> transformations = new java.util.LinkedList<AffineTransform>();
        

        // 
        transformations.push(g.getTransform());
        g.transform(new AffineTransform(0.0059701493f, 0, 0, 0.0059701493f, 0, 0));

        // _0

        // _0_0
        shape = new GeneralPath();
        ((GeneralPath) shape).moveTo(83.7, 0.0);
        ((GeneralPath) shape).curveTo(37.5, 0.0, 0.0, 37.5, 0.0, 83.7);
        ((GeneralPath) shape).curveTo(0.0, 130.0, 37.5, 167.4, 83.7, 167.4);
        ((GeneralPath) shape).curveTo(130.0, 167.4, 167.4, 129.9, 167.4, 83.7);
        ((GeneralPath) shape).curveTo(167.4, 37.5, 130.0, 0.0, 83.7, 0.0);
        ((GeneralPath) shape).closePath();
        ((GeneralPath) shape).moveTo(122.0, 120.8);
        ((GeneralPath) shape).curveTo(120.6, 123.3, 117.4, 124.0, 115.0, 122.5);
        ((GeneralPath) shape).curveTo(95.2, 110.5, 70.5, 107.8, 41.300003, 114.5);
        ((GeneralPath) shape).curveTo(38.500004, 115.0, 35.700005, 113.3, 35.100002, 110.5);
        ((GeneralPath) shape).curveTo(34.9, 107.7, 36.600002, 104.9, 39.100002, 104.3);
        ((GeneralPath) shape).curveTo(71.100006, 97.0, 98.7, 100.100006, 120.7, 113.600006);
        ((GeneralPath) shape).curveTo(123.299995, 115.100006, 124.1, 118.3, 122.5, 120.8);
        ((GeneralPath) shape).closePath();
        ((GeneralPath) shape).moveTo(132.5, 98.0);
        ((GeneralPath) shape).curveTo(130.5, 101.0, 126.5, 102.0, 123.5, 100.2);
        ((GeneralPath) shape).curveTo(101.0, 86.2, 66.7, 82.2, 40.1, 90.399994);
        ((GeneralPath) shape).curveTo(36.899998, 91.399994, 33.1, 89.399994, 32.1, 86.09999);
        ((GeneralPath) shape).curveTo(31.099998, 82.79999, 33.1, 79.09999, 36.699997, 78.09999);
        ((GeneralPath) shape).curveTo(67.1, 69.09999, 104.899994, 73.59999, 130.7, 89.09999);
        ((GeneralPath) shape).curveTo(133.7, 91.09999, 134.7, 95.09999, 132.7, 98.09999);
        ((GeneralPath) shape).closePath();
        ((GeneralPath) shape).moveTo(133.5, 74.2);
        ((GeneralPath) shape).curveTo(106.5, 58.199997, 61.9, 56.699997, 36.1, 64.5);
        ((GeneralPath) shape).curveTo(32.1, 65.8, 27.899998, 63.5, 26.599998, 59.3);
        ((GeneralPath) shape).curveTo(25.3, 55.3, 27.599998, 50.8, 31.8, 49.5);
        ((GeneralPath) shape).curveTo(61.4, 40.5, 110.600006, 42.3, 141.6, 60.7);
        ((GeneralPath) shape).curveTo(145.3, 62.9, 146.6, 67.7, 144.3, 71.4);
        ((GeneralPath) shape).curveTo(142.3, 75.200005, 137.3, 76.4, 133.7, 74.200005);
        ((GeneralPath) shape).closePath();

        g.setPaint(new Color(0x1DB954));
        g.fill(shape);

        g.setTransform(transformations.pop()); // _0

    }

    /**
     * Returns the X of the bounding box of the original SVG image.
     * 
     * @return The X of the bounding box of the original SVG image.
     */
    public static int getOrigX() {
        return 0;
    }

    /**
     * Returns the Y of the bounding box of the original SVG image.
     * 
     * @return The Y of the bounding box of the original SVG image.
     */
    public static int getOrigY() {
        return 0;
    }

    /**
     * Returns the width of the bounding box of the original SVG image.
     * 
     * @return The width of the bounding box of the original SVG image.
     */
    public static int getOrigWidth() {
        return 1;
    }

    /**
     * Returns the height of the bounding box of the original SVG image.
     * 
     * @return The height of the bounding box of the original SVG image.
     */
    public static int getOrigHeight() {
        return 1;
    }
}

