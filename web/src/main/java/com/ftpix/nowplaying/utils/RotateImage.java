package com.ftpix.nowplaying.utils;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.InvalidParameterException;

public class RotateImage {
    public static enum Direction {
        CLOCKWISE, COUNTER_CLOCKWISE, LEFT, RIGHT
    }

    ;

    private static AffineTransform rotateImageClockWise(BufferedImage image) {

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(Math.PI / 2, imageWidth / 2, imageHeight / 2);

        double offset = (imageWidth - imageHeight) / 2;
        affineTransform.translate(offset, offset);
        return affineTransform;
    }

    private static AffineTransform rotateImageCounterClockwise(BufferedImage image) {

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(-Math.PI / 2, imageWidth / 2, imageHeight / 2);

        double offset = (imageWidth - imageHeight) / 2;
        affineTransform.translate(-offset, -offset);

        return affineTransform;
    }

    public static BufferedImage rotateImage(BufferedImage bufferedImage, Direction direction) throws Exception {

        BufferedImage output = new BufferedImage(bufferedImage.getHeight(), bufferedImage.getWidth(), bufferedImage.getType());

        AffineTransform affineTransform;
        switch (direction) {
            case CLOCKWISE:
            case RIGHT:
                affineTransform = rotateImageClockWise(bufferedImage);
                break;
            case COUNTER_CLOCKWISE:
            case LEFT:
                affineTransform = rotateImageCounterClockwise(bufferedImage);
                break;
            default:
                throw new InvalidParameterException("Must give rotation direction");
        }

        AffineTransformOp affineTransformOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);
        affineTransformOp.filter(bufferedImage, output);

        return output;

    }

}
