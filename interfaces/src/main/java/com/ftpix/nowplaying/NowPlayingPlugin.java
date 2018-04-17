package com.ftpix.nowplaying;

import java.awt.*;
import java.util.List;

public interface NowPlayingPlugin extends Plugin {


    /**
     * Creates the image to be availabel as a now playing.
      * @param graphics a java graphics to draw on. NO NEED TO DISPOSE IT.
     * @param dimension the dimension of the required image
     * @param scale  scale of the image (for HiDPI screens)
     */
    void getNowPlayingImage(Graphics2D graphics, Dimension dimension, double scale) throws  Exception;
}
