package com.ftpix.nowplaying.controllers;


import com.ftpix.nowplaying.NowPlayingPlugin;
import com.ftpix.nowplaying.WebApp;
import com.ftpix.nowplaying.activities.Activity;
import com.ftpix.nowplaying.activities.MediaActivityPlugin;
import com.ftpix.nowplaying.transformers.GsonTransformer;
import com.ftpix.nowplaying.utils.PluginUtil;
import com.ftpix.sparknnotation.annotations.SparkController;
import com.ftpix.sparknnotation.annotations.SparkGet;
import com.ftpix.sparknnotation.annotations.SparkQueryParam;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Response;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SparkController("/api/now-playing")
public class NowPlayingController {
    private final Logger logger = LogManager.getLogger();

    public final static Dimension FULL_HD = new Dimension(1920, 1080);
    private final Map<Dimension, Pair<LocalDateTime, Path>> imageCache = new HashMap<>();

    /**
     * Gets the list of available  plugins
     *
     * @return
     * @throws IOException
     */
    @SparkGet(value = "/get-available-plugins", transformer = GsonTransformer.class)
    public List<Map<String, Object>> getAvailablePlugins() throws IOException {

        return PluginUtil.PLUGIN_INSTANCES
                .values()
                .stream()
                .filter(p -> p != null)
                .filter(p -> p instanceof NowPlayingPlugin)
                .map(PluginUtil.PLUGIN_TO_ID_NAME)
                .collect(Collectors.toList());
    }


    /**
     * Generates the image for the now playing
     *
     * @param res
     * @return
     */
    @SparkGet
    public Object nowPlaying(@SparkQueryParam("width") Integer width, @SparkQueryParam("height") Integer height, Response res) throws Exception {

        Dimension dimension = FULL_HD;
        if (width != null && height != null) {
            dimension = new Dimension(width, height);
        }
        logger.info("Getting now playing for dimension:{}", dimension);
        Path toUse;


        if (imageCache.containsKey(dimension)) {
            Pair<LocalDateTime, Path> localDateTimePathPair = imageCache.get(dimension);
            long timeDiff = Math.abs(ChronoUnit.SECONDS.between(localDateTimePathPair.getKey(), LocalDateTime.now()));
            logger.info("Time diff with cache = {}", timeDiff);
            if (timeDiff <= 5) {
                logger.info("Using cache instead");
                toUse = localDateTimePathPair.getValue();
            } else {
                toUse = getContent(dimension);
            }


        } else {
            toUse = getContent(dimension);
        }


        res.raw().setContentType("application/octet-stream");
        res.raw().setHeader("Content-Disposition", "inline; filename=now-playing-" + dimension.width + "x" + dimension.height + ".png");

        InputStream in = new FileInputStream(toUse.toFile());

        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                res.raw().getOutputStream().write(buffer, 0, len);
            }

            return res.raw();

        } finally {
            in.close();
        }


    }


    private Path getContent(Dimension dimension) throws Exception {

        MediaActivityPlugin mediaActivityPlugin = (MediaActivityPlugin) PluginUtil.PLUGIN_INSTANCES.get(WebApp.CONFIG.selectedActivtyPlugin);
        Activity currentActivity = mediaActivityPlugin.getCurrentActivity();

        NowPlayingPlugin nowPlayingPlugin = (NowPlayingPlugin) PluginUtil.PLUGIN_INSTANCES.get(WebApp.CONFIG.activityMapping.get(currentActivity.getId()));
        logger.info("Generating new image for Activity: [{}], and plugin:[{}]", currentActivity.getName(), nowPlayingPlugin.getName());

        BufferedImage b = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB);


        Graphics2D g =  b.createGraphics();

        nowPlayingPlugin.getNowPlayingImage(g, dimension);


        g.dispose();


        Path tempFile = Files.createTempFile("now-playing", dimension.width + "x" + dimension.height).toAbsolutePath();
        Pair<LocalDateTime, Path> pair = new Pair<>(LocalDateTime.now(), tempFile);

        logger.info("Writing to image {}", tempFile.toAbsolutePath());
        ImageIO.write(b, "png", new FileOutputStream(tempFile.toFile()));

        imageCache.put(dimension, pair);

        return tempFile;

    }
}
