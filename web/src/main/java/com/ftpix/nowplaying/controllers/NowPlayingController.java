package com.ftpix.nowplaying.controllers;


import com.ftpix.nowplaying.NowPlayingPlugin;
import com.ftpix.nowplaying.Pair;
import com.ftpix.nowplaying.Plugin;
import com.ftpix.nowplaying.WebApp;
import com.ftpix.nowplaying.activities.Activity;
import com.ftpix.nowplaying.activities.MediaActivityPlugin;
import com.ftpix.nowplaying.models.*;
import com.ftpix.nowplaying.plugins.Blackscreen;
import com.ftpix.nowplaying.transformers.GsonTransformer;
import com.ftpix.nowplaying.utils.PluginUtil;
import com.ftpix.nowplaying.utils.RotateImage;
import com.ftpix.sparknnotation.annotations.SparkController;
import com.ftpix.sparknnotation.annotations.SparkGet;
import com.ftpix.sparknnotation.annotations.SparkQueryParam;

import net.coobird.thumbnailator.filters.Rotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ftpix.nowplaying.WebApp.*;

@SparkController()
public class NowPlayingController {
    private final Logger logger = LogManager.getLogger();

    public final static Dimension FULL_HD = new Dimension(1920, 1080);

    private static ThreadLocal<Request> SERVER_REQUEST = new ThreadLocal<>();

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
    @SparkGet("/now-playing.jpg")
    public Object nowPlaying(@SparkQueryParam("width") Integer width, @SparkQueryParam("height") Integer height, @SparkQueryParam("scale") Double scale, @SparkQueryParam("rotate") String rotate, Response res, Request request) throws Exception {
        SERVER_REQUEST.set(request);

        Dimension dimension = FULL_HD;
        if (width != null && height != null) {
            dimension = new Dimension(width, height);
        }

        scale = Optional.ofNullable(scale).orElse(1D);
        logger.info("Getting now playing for dimension:{} and scale {}", dimension, scale);

        byte[] toUse = getContent(dimension, scale, rotate != null && rotate.trim().length() > 0 ? RotateImage.Direction.valueOf(rotate.toUpperCase()) : null);


        res.raw().setContentType("application/octet-stream");
        res.raw().setHeader("Content-Disposition", "inline; filename=now-playing-" + dimension.width + "x" + dimension.height + "x" + scale + "r" + rotate + ".jpg");


        res.raw().getOutputStream().write(toUse);
        return res.raw();


    }


    private NowPlayingPlugin getDefaultNowPlaying() {
        return (NowPlayingPlugin) PluginUtil.PLUGIN_INSTANCES.get(Blackscreen.BLACK_SCREEN_PLUGIN_ID);
    }

    /**
     * Gets the current activty based on the given flow
     *
     * @param node
     * @return
     */
    public NowPlayingPlugin processFlow(FlowNode node) throws Exception {

        logger.info("Processing flow node: {}", node);
        if (node == null) {
            logger.info("node is null");
            return getDefaultNowPlaying();
        }
        ActivityNode activity = node.getActivity();
        RequestFlowNode request = node.getRequest();
        String nowPlaying = node.getNowPlaying();

        if (activity == null && nowPlaying == null && request == null) {
            logger.info("node doesn't have options");
            return getDefaultNowPlaying();
        }

        if (nowPlaying != null) {
            logger.info("nowPlaying node");
            return (NowPlayingPlugin) PluginUtil.PLUGIN_INSTANCES.get(nowPlaying);
        }

        if (activity != null) {
            logger.info("activity node");
            if (activity.getNodes() == null || activity.getNodes().size() == 0) {
                logger.info("activity nodes are either null or empty");
                return getDefaultNowPlaying();
            }
            //finding the current activity
            MediaActivityPlugin plugin = (MediaActivityPlugin) PluginUtil.PLUGIN_INSTANCES.get(activity.getPlugin());
            String currentActivity = plugin.getCurrentActivity().getId();

            return Optional.ofNullable(currentActivity)
                    .filter(a -> activity.getNodes().containsKey(a))
                    .map(a -> activity.getNodes().get(a))
                    .filter(Objects::nonNull)
                    .map(n -> {
                        try {
                            return processFlow(n);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .orElseGet(this::getDefaultNowPlaying);

        }


        if (request != null) {
            logger.info("request node");
            RequestFlowNodeOption requestOption = getRequestOption(request, SERVER_REQUEST.get());
            if (requestOption != null) {
                return processFlow(requestOption.getValue());
            } else {
                return getDefaultNowPlaying();
            }
        }


        return getDefaultNowPlaying();
    }

    private RequestFlowNodeOption getRequestOption(RequestFlowNode node, Request request) {

        //get an option by name or else get teh default one
        Function<String, Optional<RequestFlowNodeOption>> getForName = s -> node.getOptions().stream()
                .filter(o -> o.getName().equalsIgnoreCase(s))
                .findFirst();

        String value = null;
        if (request != null) {
            switch (node.getType()) {
                case "queryParam":
                    value = request.queryParams(node.getName());
                    break;
                default:
                    value = "default";
            }
        }


        if (value == null) {
            value = "default";
        }

        return Optional.ofNullable(value)
                .flatMap(getForName)
                .orElse(null);


    }


    private byte[] getContent(Dimension dimension, double scale, RotateImage.Direction direction) throws Exception {


        int scaledWidth = (int) ((double) dimension.width * scale);
        int scaledHeight = (int) ((double) dimension.height * scale);
        Dimension scaledDimension = new Dimension(scaledWidth, scaledHeight);


        String cacheIndex = dimensionsToString(dimension, scale);

        try {

            NowPlayingPlugin nowPlayingPlugin = processFlow(CONFIG.flow);

            Object nowPlayingContent = nowPlayingPlugin.getNowPlayingContent();

            //checking cache, if the object is equal we can skip the image creation
            BufferedImage b = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_3BYTE_BGR);
//                BufferedImage b = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = b.createGraphics();
            try {
                RenderingHints rh = new RenderingHints(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setRenderingHints(rh);


                logger.info("Generating new image for plugin:[{}]", nowPlayingPlugin.getName());

                long now = System.currentTimeMillis();
                nowPlayingPlugin.getNowPlayingImage(nowPlayingContent, g, scaledDimension, scale);


                logger.info("Plugin {} created image in {}ms", nowPlayingPlugin.getName(), System.currentTimeMillis() - now);

                if (direction != null) {
                    b = RotateImage.rotateImage(b, direction);
                    logger.info("Picture rotated by {} degrees, new width: {}px height: {}px", direction, b.getWidth(), b.getHeight());
                    ImageIO.write(b, "jpg", new File("./test.jpg"));
                }


                byte[] bytes = bufferedImagetoBytes(b);

                return bytes;
            } finally {
                g.dispose();
            }

        } catch (Exception e) {
            logger.error("Couldn't generate image", e);
            return generateErrorImage(scaledDimension);
        }


    }

    private byte[] generateErrorImage(Dimension d) {
        BufferedImage b = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = b.createGraphics();
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHints(rh);

        Dimension onePercent = new Dimension(d.width / 100, d.height / 100);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, onePercent.height * 8));
        g.setColor(Color.orange);
        g.drawString("/!\\", onePercent.width * 10, onePercent.height * 10);
        g.dispose();


        try {
            return bufferedImagetoBytes(b);
        } catch (IOException e) {
            logger.error("Couldn't even create error image... shit hit the fan", e);
            return null;
        }
    }

    /**
     * Creates a byte array from a buffered image
     *
     * @param b
     * @return
     * @throws IOException
     */
    private byte[] bufferedImagetoBytes(BufferedImage b) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            ImageIO.write(b, "JPG", os);
            return os.toByteArray();
        } finally {
        }

    }

    private String dimensionsToString(Dimension d, double scale) {
        return d.width + "x" + d.height + "x" + scale;
    }

}
