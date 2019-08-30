package com.ftpix.plugin.plex;

import com.ftpix.nowplaying.NowPlayingPlugin;
import com.ftpix.nowplaying.Setting;
import com.ftpix.nowplaying.SettingType;
import com.ftpix.nowplaying.Utils;
import com.ftpix.nowplaying.activities.Activity;
import com.ftpix.nowplaying.activities.MediaActivityPlugin;
import com.ftpix.plugin.plex.model.PlexSession;
import com.ftpix.plugin.plex.model.Video;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class Plex implements NowPlayingPlugin<Video>, MediaActivityPlugin {

    private static final String PLEX_SESSIONS_URL = "%sstatus/sessions?X-Plex-Token=%s",
            PLEX_DECK_URL = "%slibrary/onDeck?X-Plex-Token=%s",
            PLEX_ART_URL = "%s%s?X-Plex-Token=%s",
            PLEX_HEADER_ACCEPT = "Accept",
            PLEX_HEADER_ACCEPT_VALUE = "application/json";
    private static final String SETTINGS_URL = "url";
    private static final String SETTINGS_TOKEN = "token";
    private static final String SETTINGS_PLAYER = "player";

    private String url;
    private String token;
    private String player;
    private final Logger logger = LogManager.getLogger();

    @Override
    public Video getNowPlayingContent() throws Exception {
        return getNowPlaying();
    }

    @Override
    public void getNowPlayingImage(Video video, Graphics2D graphics, Dimension dimension, double scale) throws Exception {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dimension.width, dimension.height);

        if (video != null) {
            Dimension onePercent = new Dimension(dimension.width / 100, dimension.height / 100);

            Map<String, BufferedImage> images = setArtAndThumbnail(video);
            BufferedImage art = images.get("art");
            int currentX = 0;
            int currentY = 0;

            int fontSize = onePercent.height * 8;

            if (art != null) {
                BufferedImage background = Utils.cover(art, dimension);
                graphics.drawImage(background, null, dimension.width / 2 - background.getWidth() / 2, dimension.height / 2 - background.getHeight() / 2);

                graphics.setColor(new Color(0, 0, 0, 0.7F));
                graphics.fillRect(0, 0, dimension.width, dimension.height);

            }

            BufferedImage thumb = images.get("thumb");
            if (thumb != null) {
                BufferedImage scaledThumb = Thumbnails.of(thumb).size(onePercent.width * 30, onePercent.height * 90).asBufferedImage();
                scaledThumb = Utils.makeRoundedCorner(Thumbnails.of(scaledThumb).size(dimension.height / 2, dimension.height / 2).asBufferedImage(), 20);
                graphics.drawImage(scaledThumb, null, onePercent.width * 10, dimension.height / 2 - scaledThumb.getHeight() / 2);

                currentX = onePercent.width * 10 + scaledThumb.getWidth() + onePercent.width * 5;
                currentY = dimension.height / 2 - scaledThumb.getHeight() / 2;
            }

            graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
            int maxTextWidth = dimension.width - currentX - onePercent.width * 10;
            int lineHeight = (int) graphics.getFont().getStringBounds("ABCDEFGHIJKLMNOPQR", graphics.getFontRenderContext()).getHeight();
            int percentHeightSpacing = 7 * onePercent.height;

            graphics.setColor(Color.WHITE);
            List<String> texts = new ArrayList<>();

            if (video.type.equalsIgnoreCase("episode")) {
                if (video.grandparentTitle != null && video.grandparentTitle.trim().length() > 0) {
                    texts.add(video.grandparentTitle);
                }

                if (video.parentTitle != null && video.parentTitle.trim().length() > 0) {
                    String text = video.parentTitle;
                    if (video.index != null && video.index.trim().length() > 0) {
                        text += " - Episode " + video.index;
                    }
                    texts.add(text);
                }

                if (video.title != null && video.title.trim().length() > 0) {
                    texts.add(video.title);
                }

            } else {
                texts.add(video.title);
                if (video.year != null && video.year.trim().length() > 0) {
                    texts.add(video.year);
                }
            }

            //now displaying texts
            int fullHeight = texts.size() * lineHeight + texts.size() * percentHeightSpacing;
            System.out.println(fullHeight);
            int startFrom = dimension.height / 2 - fullHeight / 2 + lineHeight;
            currentY = startFrom;

            for (String t : texts) {
                drawString(t, fontSize, maxTextWidth, graphics, currentX, currentY);
                currentY += lineHeight + percentHeightSpacing;
            }


        } else {
            //if no content, let's draw the plex logo

            List<Video> deck = getOnDeck();

            if (deck.size() > 0) {
                BufferedImage mosaic = buildMosaic(deck, dimension);

                graphics.drawImage(mosaic, dimension.width / 2 - mosaic.getWidth() / 2, dimension.height / 2 - mosaic.getHeight() / 2, null);
            }

            graphics.setColor(new Color(0, 0, 0, 0.5f));
            graphics.fillRect(0, 0, dimension.width, dimension.height);

            Dimension fiftyPercent = Utils.getPercentOf(dimension, 50);
            int iconScale = Math.min(fiftyPercent.width, fiftyPercent.height);

            graphics.translate(dimension.width / 2 - iconScale / 2, dimension.height / 2 - iconScale / 2);
            graphics.scale(iconScale, iconScale);
            Icon.paint(graphics);
        }

    }


    private BufferedImage bestFitMosaic(List<BufferedImage> images, Dimension dimension) throws IOException {
        int rows = findMosaicRowCount(images, dimension, 1);


        int imageHeight = dimension.height / rows;

        BufferedImage image = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        int imageIndex = 0;
        int y = 0;
        int maxWwidth = 0;
        for (int row = 0; row < rows; row++) {
            int x = 0;

            while (x <= dimension.width && imageIndex < images.size()) {
                BufferedImage b = Thumbnails.of(images.get(imageIndex)).height(imageHeight).asBufferedImage();
                g.drawImage(b, x, y, null);
                x += b.getWidth();
                maxWwidth = Math.max(maxWwidth, x);
                imageIndex++;
            }

            y += imageHeight;
        }
        if (maxWwidth < dimension.width) {
            return image.getSubimage(0, 0, maxWwidth, imageHeight);
        } else {
            return image;
        }
    }

    private int findMosaicRowCount(List<BufferedImage> images, Dimension dimension, int rows) {
        int maxImageHeight = dimension.height / rows;

        int currentWidth = 0;
        int count = 0;
        int maxCount = 0;
        int currentRow = 0;

        for (BufferedImage image : images) {
            double scale = (double) maxImageHeight / (double) image.getHeight();
            int newWidth = (int) (image.getWidth() * scale);
            currentWidth += newWidth;
            count++;

            if (currentWidth >= dimension.width) {
                maxCount = Math.max(maxCount, count);
                count = 0;
                currentWidth = 0;
                currentRow++;

                if (currentRow > rows) {
                    //we still have space to put more images
                    return findMosaicRowCount(images, dimension, ++rows);
                }

            }
        }

        if (rows > 1) {
            return rows - 1;
        } else {
            return rows;
        }
    }


    /**
     * Builds a single image based on the list of videos
     *
     * @param videos
     * @param dimension
     * @return
     */
    private BufferedImage buildMosaic(List<Video> videos, Dimension dimension) throws IOException {
        int currentWidth = 0;

        List<BufferedImage> images = new ArrayList<>();

        for (Video video : videos) {
            BufferedImage b = getVideoThumb(video);
            b = Thumbnails.of(b).height(dimension.height).asBufferedImage();
            if (b != null) {
                images.add(b);
                currentWidth += b.getWidth();
            }
        }


        return Utils.cover(bestFitMosaic(images, dimension), dimension);


    }

    /**
     * Gets on deck videos
     *
     * @return
     */
    private List<Video> getOnDeck() throws UnirestException {

        String toCall = String.format(PLEX_DECK_URL, url, token);

        GetRequest get = Unirest.get(toCall).header(PLEX_HEADER_ACCEPT, PLEX_HEADER_ACCEPT_VALUE);
        JsonNode response = get.asJson().getBody();
        logger.info("Response: {}", response);

        PlexSession sessions = PlexResultParser.parseJson(response);
        if (sessions.getMediaContainer().videos.isEmpty()) {
            return null;
        }


        return sessions.getMediaContainer().videos;
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
    private void drawString(String text, int wantedFontSize, int maxLength, Graphics2D graphics, int x, int y) {
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
     * Generates the thumbnail and art depending of availability;
     * do not download and generate if playing is still the same
     *
     * @param video
     */
    private Map<String, BufferedImage> setArtAndThumbnail(Video video) throws IOException {
        String art = Optional.ofNullable(video.art)
                .filter(a -> a.trim().length() > 0)
                .orElse(
                        Optional.ofNullable(video.parentArt)
                                .filter(a -> a.trim().length() > 0)
                                .orElse(
                                        Optional.ofNullable(video.grandparentArt)
                                                .filter(a -> a.trim().length() > 0)
                                                .orElse("")
                                )
                );

        String artUrl = art.trim().length() > 0 ? String.format(PLEX_ART_URL, url.substring(0, url.length() - 1), art, token) : null;


        Map<String, BufferedImage> values = new HashMap<>();

        values.put("art", ImageIO.read(new URL(artUrl)));
        values.put("thumb", getVideoThumb(video));

        return values;
    }


    private BufferedImage getVideoThumb(Video video) throws IOException {
        String thumbUrl = null;
        if (video.type.equalsIgnoreCase("episode")) {
            String thumb = Optional.ofNullable(video.grandparentThumb)
                    .filter(a -> a.trim().length() > 0)
                    .orElse(
                            Optional.ofNullable(video.parentThumb)
                                    .filter(a -> a.trim().length() > 0)
                                    .orElse(
                                            Optional.ofNullable(video.thumb)
                                                    .filter(a -> a.trim().length() > 0)
                                                    .orElse("")
                                    )
                    );

            thumbUrl = thumb.trim().length() > 0 ? String.format(PLEX_ART_URL, url.substring(0, url.length() - 1), thumb, token) : null;

        } else {
            String thumb = Optional.ofNullable(video.thumb)
                    .filter(a -> a.trim().length() > 0)
                    .orElse(
                            Optional.ofNullable(video.parentThumb)
                                    .filter(a -> a.trim().length() > 0)
                                    .orElse(
                                            Optional.ofNullable(video.grandparentThumb)
                                                    .filter(a -> a.trim().length() > 0)
                                                    .orElse("")
                                    )
                    );

            thumbUrl = thumb.trim().length() > 0 ? String.format(PLEX_ART_URL, url.substring(0, url.length() - 1), thumb, token) : null;
        }

        return ImageIO.read(new URL(thumbUrl));
    }

    @Override
    public String getName() {
        return "Plex";
    }

    @Override
    public String getId() {
        return "com.ftpix.plugins.plex";
    }

    @Override
    public void init(Map<String, String> settings) {

        url = settings.get(SETTINGS_URL);
        token = settings.get(SETTINGS_TOKEN);
        player = settings.get(SETTINGS_PLAYER);

        if (!url.startsWith("http")) {
            url = "http://" + url;
        }

        if (!url.endsWith("/")) {
            url += "/";
        }
    }

    @Override
    public List<String> validateSettings(Map<String, String> settings) {
        String url = settings.get(SETTINGS_URL);
        String token = settings.get(SETTINGS_TOKEN);

        if (!url.startsWith("http")) {
            url = "http://" + url;
        }

        if (!url.endsWith("/")) {
            url += "/";
        }

        String toCall = String.format(PLEX_SESSIONS_URL, url, token);

        logger.info("Testing setting with url:[{}]", toCall);

        try {
            GetRequest get = Unirest.get(toCall)
                    .header(PLEX_HEADER_ACCEPT, PLEX_HEADER_ACCEPT_VALUE);

            PlexSession sessions = PlexResultParser.parseJson(get.asJson().getBody());
            return null;
        } catch (UnirestException e) {
            List<String> errors = new ArrayList<>();
            errors.add("Connection failed: Cound't connect to server: " + e.getMessage());
            return errors;
        }
    }

    @Override
    public List<Setting> getSettings() {
        Setting url = new Setting();
        url.setType(SettingType.TEXT);
        url.setName(SETTINGS_URL);
        url.setLabel("Server URL");

        Setting token = new Setting();
        token.setType(SettingType.TEXT);
        token.setName(SETTINGS_TOKEN);
        token.setLabel("Authentication token");

        Setting player = new Setting();
        player.setType(SettingType.TEXT);
        player.setName(SETTINGS_PLAYER);
        player.setLabel("Preferred Plex Client name");

        return List.of(url, token, player);
    }

    @Override
    public void stop() {

    }


    /**
     * Gets the currently playing video if any
     *
     * @return
     * @throws UnirestException
     */
    public Video getNowPlaying() throws UnirestException {
        String toCall = String.format(PLEX_SESSIONS_URL, url, token);

        GetRequest get = Unirest.get(toCall).header(PLEX_HEADER_ACCEPT, PLEX_HEADER_ACCEPT_VALUE);
        JsonNode response = get.asJson().getBody();
        logger.info("Response: {}", response);
        PlexSession sessions = PlexResultParser.parseJson(response);
        if (sessions.getMediaContainer().videos.isEmpty()) {
            return null;
        }
        return sessions.getMediaContainer().videos
                .stream()
                .filter(v -> player != null && player.trim().length() > 0 && player.equalsIgnoreCase(v.player.title))
                .findFirst()
                .orElse(sessions.getMediaContainer().videos.get(0));

    }

    @Override
    public List<Activity> getActivities() throws Exception {
        Activity activity = new Activity();
        activity.setName("Playing");
        activity.setId("playing");

        Activity activity2 = new Activity();
        activity2.setName("Stopped");
        activity2.setId("stopped");
        return List.of(activity, activity2);
    }

    @Override
    public Activity getCurrentActivity() throws Exception {
        Activity playing = new Activity();
        playing.setName("Playing");
        playing.setId("playing");

        Activity notPlaying = new Activity();
        notPlaying.setName("Stopped");
        notPlaying.setId("stopped");
        try {
            Video nowPlaying = getNowPlaying();
            if (nowPlaying != null) {
                return playing;
            }
        } catch (Exception e) {
            logger.error("Couldn't get plex now playing:", e);
        }

        return notPlaying;
    }
}
