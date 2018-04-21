package com.ftpix.plugin.plex;

import com.ftpix.nowplaying.NowPlayingPlugin;
import com.ftpix.nowplaying.Setting;
import com.ftpix.nowplaying.SettingType;
import com.ftpix.nowplaying.Utils;
import com.ftpix.plugin.plex.model.PlexSession;
import com.ftpix.plugin.plex.model.Video;
import com.jhlabs.image.BoxBlurFilter;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.Resizers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.css.Rect;

import javax.imageio.ImageIO;
import javax.management.OperationsException;
import javax.swing.*;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Plex implements NowPlayingPlugin {

    private static final String PLEX_SESSIONS_URL = "%sstatus/sessions?X-Plex-Token=%s",
            PLEX_ART_URL = "%s%s?X-Plex-Token=%s",
            PLEX_HEADER_ACCEPT = "Accept",
            PLEX_HEADER_ACCEPT_VALUE = "application/json";
    private static final String SETTINGS_URL = "url";
    private static final String SETTINGS_TOKEN = "token";
    private static final String SETTINGS_PLAYER = "player";
    private BufferedImage art, thumb;

    private String url;
    private String token;
    private String player;
    private Video nowPlaying;
    private final Logger logger = LogManager.getLogger();

    @Override
    public void getNowPlayingImage(Graphics2D graphics, Dimension dimension, double scale) throws Exception {
        Video video = getNowPlaying();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dimension.width, dimension.height);

        if (video != null) {
            Dimension onePercent = new Dimension(dimension.width / 100, dimension.height / 100);

            setArtAndThumbnail(video);
            int currentX = 0;
            int currentY = 0;

            int fontSize = onePercent.height * 8;

            if (art != null) {
                Thumbnails.Builder<BufferedImage> thumb = Thumbnails.of(art);
                double artRatio = (double) art.getWidth() / (double) art.getHeight();
                double screenRatio = (double) dimension.width / (double) dimension.height;
                if (artRatio > screenRatio) {
                    thumb = thumb.height(dimension.height);
                } else {
                    thumb = thumb.width(dimension.width);
                }

                BufferedImage background = thumb.asBufferedImage();
                graphics.drawImage(background, null, dimension.width / 2 - background.getWidth() / 2, dimension.height / 2 - background.getHeight() / 2);

                graphics.setColor(new Color(0, 0, 0, 0.7F));
                graphics.fillRect(0, 0, dimension.width, dimension.height);

            }
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


        }

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
    private void setArtAndThumbnail(Video video) throws IOException {
        if (nowPlaying == null || !video.key.equalsIgnoreCase(nowPlaying.key)) {
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

            String artUrl = art.trim().length() > 0?String.format(PLEX_ART_URL, url.substring(0, url.length() - 1), art, token):null;

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

                thumbUrl = thumb.trim().length() > 0 ?String.format(PLEX_ART_URL, url.substring(0, url.length() - 1), thumb, token):null;

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

                thumbUrl = thumb.trim().length() > 0?String.format(PLEX_ART_URL, url.substring(0, url.length() - 1), thumb, token):null;
            }


            this.art = ImageIO.read(new URL(artUrl));
            this.thumb = ImageIO.read(new URL(thumbUrl));

            nowPlaying = video;

        } else {
            logger.info("GEtting images from cache");
        }
    }

    @Override
    public String getName() {
        return "plex";
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
}
