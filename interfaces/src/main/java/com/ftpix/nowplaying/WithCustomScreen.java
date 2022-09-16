package com.ftpix.nowplaying;

import java.awt.*;
import java.util.List;
import java.util.Map;

public interface WithCustomScreen<T> {
    List<String> getTemplateVariables();

    /**
     * Creates the image to be availabel as a now playing.
     *
     * @param graphics  a java graphics to draw on. NO NEED TO DISPOSE IT.
     * @param dimension the dimension of the required image
     * @param scale     scale of the image (for HiDPI screens)
     */
    void getNowPlayingImageForScreen(T content, Graphics2D graphics, Dimension dimension, Map<String, TemplateVariable> variables) throws Exception;
}
