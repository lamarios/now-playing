package com.ftpix.nowplaying;

import java.awt.*;
import java.util.List;

public interface NowPlayingPlugin<T> extends Plugin {


    /**
     * Get the currently playing content. It can e anything.
     * Make sure it implementa equal so that for example on aspotify plugin, a song should be equal to itself
     * @return
     */
    T getNowPlayingContent() throws Exception;
    /**
     * Creates the image to be availabel as a now playing.
      * @param graphics a java graphics to draw on. NO NEED TO DISPOSE IT.
     * @param dimension the dimension of the required image
     * @param scale  scale of the image (for HiDPI screens)
     */
    void getNowPlayingImage(T content, Graphics2D graphics, Dimension dimension, double scale) throws  Exception;
}
