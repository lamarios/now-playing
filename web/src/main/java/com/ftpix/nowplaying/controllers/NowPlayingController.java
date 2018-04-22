package com.ftpix.nowplaying.controllers;


import com.ftpix.nowplaying.NowPlayingPlugin;
import com.ftpix.nowplaying.Pair;
import com.ftpix.nowplaying.WebApp;
import com.ftpix.nowplaying.activities.Activity;
import com.ftpix.nowplaying.activities.MediaActivityPlugin;
import com.ftpix.nowplaying.transformers.GsonTransformer;
import com.ftpix.nowplaying.utils.PluginUtil;
import com.ftpix.sparknnotation.annotations.SparkController;
import com.ftpix.sparknnotation.annotations.SparkGet;
import com.ftpix.sparknnotation.annotations.SparkQueryParam;

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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@SparkController()
public class NowPlayingController {
    private final Logger logger = LogManager.getLogger();

    public final static Dimension FULL_HD = new Dimension(1920, 1080);
    private final Map<Pair<Dimension, Double>, Pair<LocalDateTime, byte[]>> imageCache = new ConcurrentHashMap<>();
    private ExecutorService cacheCleaner = Executors.newSingleThreadExecutor();

    public NowPlayingController() {
        clearCache();
    }

    /**
     * Gets the list of available  plugins
     *
     * @return
     * @throws IOException
     */
    @SparkGet(value = "/api/now-playing/get-available-plugins", transformer = GsonTransformer.class)
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
    @SparkGet("/now-playing.png")
    public Object nowPlaying(@SparkQueryParam("width") Integer width, @SparkQueryParam("height") Integer height, @SparkQueryParam("scale") Double scale, Response res) throws Exception {

        Dimension dimension = FULL_HD;
        if (width != null && height != null) {
            dimension = new Dimension(width, height);
        }

        scale = Optional.ofNullable(scale).orElse(1D);
        logger.info("Getting now playing for dimension:{} and scale {}", dimension, scale);
        BufferedImage toUse;

        Pair<Dimension, Double> cacheEntry = new Pair<>(dimension, scale);
        if (imageCache.containsKey(cacheEntry)) {
            Pair<LocalDateTime,byte[]> localDateTimePathPair = imageCache.get(cacheEntry);
            long timeDiff = Math.abs(ChronoUnit.SECONDS.between(localDateTimePathPair.getKey(), LocalDateTime.now()));
            logger.info("Time diff with cache = {}", timeDiff);
            if (timeDiff <= 5) {
                logger.info("Using cache instead");
                byte[] bytes  = localDateTimePathPair.getValue();
                res.raw().getOutputStream().write(bytes);

                return res.raw();
            } else {
                toUse = getContent(dimension, scale);
            }


        } else {
            toUse = getContent(dimension, scale);
        }


        res.raw().setContentType("application/octet-stream");
        res.raw().setHeader("Content-Disposition", "inline; filename=now-playing-" + dimension.width + "x" + dimension.height + "x" + scale + ".png");


        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(toUse, "png", os);

            Pair<LocalDateTime, byte[]> pair = new Pair<>(LocalDateTime.now(), os.toByteArray());
            imageCache.put(new Pair<>(dimension, scale), pair);

            res.raw().getOutputStream().write(pair.getValue());
            return res.raw();

        } finally {
        }


    }


    private BufferedImage getContent(Dimension dimension, double scale) throws Exception {


        int scaledWidth = (int) ((double) dimension.width * scale);
        int scaledHeight = (int) ((double) dimension.height * scale);
        Dimension scaledDimension = new Dimension(scaledWidth, scaledHeight);


        BufferedImage b = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = b.createGraphics();
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHints(rh);

        try {
            MediaActivityPlugin mediaActivityPlugin = (MediaActivityPlugin) PluginUtil.PLUGIN_INSTANCES.get(WebApp.CONFIG.selectedActivtyPlugin);
            Activity currentActivity = mediaActivityPlugin.getCurrentActivity();

            NowPlayingPlugin nowPlayingPlugin = (NowPlayingPlugin) PluginUtil.PLUGIN_INSTANCES.get(WebApp.CONFIG.activityMapping.get(currentActivity.getId()));
            logger.info("Generating new image for Activity: [{}], and plugin:[{}]", currentActivity.getName(), nowPlayingPlugin.getName());

            long now = System.currentTimeMillis();
            nowPlayingPlugin.getNowPlayingImage(g, scaledDimension, scale);


            logger.info("Plugin {} created image in {}ms", nowPlayingPlugin.getName(), System.currentTimeMillis() - now);



        } catch (Exception e) {
            logger.error("Couldn't generate image", e);
            Dimension onePercent = new Dimension(dimension.width / 100, dimension.height / 100);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, onePercent.height * 8));
            g.setColor(Color.orange);
            g.drawString("/!\\", onePercent.width * 10, onePercent.height * 10);
        } finally {
            g.dispose();
        }

        return b;

    }

    private void clearCache() {
        cacheCleaner.execute(() -> {
            while (true) {
                logger.info("Clearing image cache");
                LocalDateTime now = LocalDateTime.now();

                imageCache.keySet()
                        .stream()
                        .filter(k -> {
                            long diff = Math.abs(ChronoUnit.SECONDS.between(now, imageCache.get(k).getKey()));
                            return diff > 30;
                        })
                        .forEach(k -> {
                            logger.info("Removing from cache {}", k);
                            imageCache.remove(k);
                        });


                logger.info("new cache size:{}", imageCache.size());

                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
