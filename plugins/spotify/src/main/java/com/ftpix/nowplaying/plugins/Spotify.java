package com.ftpix.nowplaying.plugins;

import com.ftpix.nowplaying.*;
import com.ftpix.nowplaying.activities.Activity;
import com.ftpix.nowplaying.activities.MediaActivityPlugin;
import com.ftpix.nowplaying.plugins.models.Error;
import com.ftpix.nowplaying.plugins.models.Image;
import com.ftpix.nowplaying.plugins.models.SpotifyNowPlaying;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jhlabs.image.BoxBlurFilter;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;
import javafx.util.Pair;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Spark;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Spotify implements NowPlayingPlugin, MediaActivityPlugin, ExternalLoginPlugin {
    private final Logger logger = LogManager.getLogger();
    private SpotifyToken token;
    public static final String SPOTIFY_API_TOKEN = "https://accounts.spotify.com/api/token";
    public static final String SPOTIFY_CURRENTLY_PLAYING = "http://api.spotify.com/v1/me/player/currently-playing";
    public static final String MESSAGE_TOKEN_EXPIRED = "The access token expired";
    private static final String SETTINGS_CLIENT_SECRET = "clientSecret";
    private static final String SETTINGS_CLIENT_ID = "clientId";
    private String clientSecret;
    private String clientID;
    private String currentlyPlaying;
    private BufferedImage art, blurredArt;


    private final Gson gson = new GsonBuilder().create();
    private boolean loggedIn = false;

    @Override
    public void getNowPlayingImage(Graphics2D graphics, Dimension dimension, double scale) throws Exception {
        Dimension onePercent = new Dimension(dimension.width / 100, dimension.height / 100);

        SpotifyNowPlaying nowPlaying = getNowPlaying();
        Pair<BufferedImage, BufferedImage> nowPlayingImage = getNowPlayingImage(nowPlaying);
        BufferedImage albumArt = nowPlayingImage.getKey();
        BufferedImage background = nowPlayingImage.getValue();


        //black background
        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, dimension.width, dimension.height);

        //building the background image
        int maxSize = Math.max(dimension.width, dimension.height);
        background = Thumbnails.of(background).size(maxSize, maxSize).asBufferedImage();
        graphics.drawImage(background, null, dimension.width / 2 - background.getWidth() / 2, dimension.height / 2 - background.getHeight() / 2);

        //semi transparent layer
        graphics.setColor(new Color(0, 0, 0, 0.7F));
        graphics.fillRect(0, 0, dimension.width, dimension.height);


        //drawing art
        albumArt = Utils.makeRoundedCorner(Thumbnails.of(albumArt).size(dimension.height / 2, dimension.height / 2).asBufferedImage(), 20);

        int imageX = onePercent.width * 5;
        int imageY = dimension.height / 2 - albumArt.getHeight() / 2;
        int percentHeightSpacing = 7;
        graphics.drawImage(albumArt, null, imageX, imageY);

        //drawing names
        int fontSize = onePercent.height * 8;
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        graphics.setColor(Color.WHITE);

        //drawing song name
        int lineHeight = (int) graphics.getFont().getStringBounds(nowPlaying.item.name, graphics.getFontRenderContext()).getHeight();
        int textCurrentY = imageY + lineHeight;
        int textCurrentX = imageX + albumArt.getWidth() + onePercent.width * 2;


        int textMaxWidth = dimension.width -  textCurrentX - onePercent.width * 5;
        drawString(nowPlaying.item.name, fontSize, textMaxWidth, graphics, textCurrentX, textCurrentY);


        //drawing album
        textCurrentY += lineHeight + onePercent.height * percentHeightSpacing;
        drawString(nowPlaying.item.album.name, fontSize, textMaxWidth, graphics, textCurrentX, textCurrentY);

        //drawing  artists
        String artists = nowPlaying.item.artists.stream().map(s -> s.name).collect(Collectors.joining(", "));
        textCurrentY += lineHeight + onePercent.height * percentHeightSpacing;
        drawString(artists, fontSize, textMaxWidth, graphics, textCurrentX, textCurrentY);

        textCurrentY += onePercent.height * percentHeightSpacing;
        //progress bar
        int barLength = dimension.width - textCurrentX - onePercent.width * 5;
        int barHeight = onePercent.height * 2;
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(textCurrentX, textCurrentY, barLength, barHeight);
        double progress = (double) barLength * (((double) nowPlaying.progress_ms / (double) nowPlaying.item.duration_ms));
        graphics.setColor(Color.WHITE);
        graphics.fillRect(textCurrentX, textCurrentY, (int) progress, barHeight);

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
     * Gets the currently playing image
     * @param nowPlaying
     * @return
     * @throws IOException
     */
    private Pair<BufferedImage, BufferedImage> getNowPlayingImage(SpotifyNowPlaying nowPlaying) throws IOException {
        if (art != null
                && blurredArt != null
                && (currentlyPlaying != null && nowPlaying.item.id.equalsIgnoreCase(currentlyPlaying))) {
            //loading from file
            logger.info("Loading album art from cache");
            return new Pair<>(art, blurredArt);
        } else {
            //we need to download it
            logger.info("Downloading album art");
            BufferedImage art = nowPlaying.item.album.images
                    .stream()
                    .sorted(Comparator.comparingInt((Image value) -> value.width).reversed())
                    .findFirst()
                    .map(i -> {
                        try {
                            return ImageIO.read(new URL(i.url));
                        } catch (IOException e) {
                            logger.error("Couldn't get image from Spotify");
                            return null;
                        }
                    })
                    .filter(i -> i != null)
                    .orElseThrow(() -> new IOException("This song doesn't have image"));

            BufferedImage blurred = new BoxBlurFilter(100, 100, 1).filter(art, null);

            this.currentlyPlaying = nowPlaying.item.id;
            this.art = art;
            this.blurredArt = blurred;


            return new Pair<>(art, blurred);

        }


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
            SpotifyNowPlaying spotifyNowPlaying = getNowPlaying();
            if (spotifyNowPlaying.is_playing) {
                return playing;
            }
        } catch (Exception e) {
            logger.error("Couldn't get spotify now playing:", e);
        }

        return notPlaying;
    }

    @Override
    public void stop() {

    }

    @Override
    public String getName() {
        return "Spotify";
    }

    @Override
    public String getId() {
        return "com.ftpix.nowplaying.plugins.Spotify";
    }

    @Override
    public void init(Map<String, String> settings) {
        clientSecret = settings.get(SETTINGS_CLIENT_SECRET);
        clientID = settings.get(SETTINGS_CLIENT_ID);

        token = loadToken();
    }

    @Override
    public List<String> validateSettings(Map<String, String> settings) {
        return null;
    }

    @Override
    public List<Setting> getSettings() {

        Setting clientId = new Setting();
        clientId.setType(SettingType.TEXT);
        clientId.setName("clientId");
        clientId.setLabel("Client Id");


        Setting clientSecret = new Setting();
        clientSecret.setType(SettingType.TEXT);
        clientSecret.setName("clientSecret");
        clientSecret.setLabel("Client Secret");


        return List.of(clientId, clientSecret);
    }

    @Override
    public List<ExternalEndPointDefinition> defineExternalEndPoints() {
        ExternalEndPointDefinition authorise = new ExternalEndPointDefinition();

        authorise.setMethod(ExternalEndPointDefinition.Method.GET);
        authorise.setUrl("/authorize");
        authorise.setRoute(this::handleAuthorization);

        return List.of(authorise);
    }

    private String handleAuthorization(Request req, Response resp) {
//        logger.info("Received authorization request");

        Optional<String> error = Optional.ofNullable(req.queryParams("error"));
        Optional<String> code = Optional.ofNullable(req.queryParams("code")).filter(c -> c.length() > 0);
        Optional<String> state = Optional.ofNullable(req.queryParams("state"));

        if (error.isPresent()) {
            Spark.halt(error.get());
        }

        code.ifPresent(c -> {
            //We have a code we need to get the token
            getToken(c, state.orElse("")).ifPresent(spotifyToken -> {
                token = spotifyToken;
                logger.info("Recieved token with refresh token [{}]", token.refresh_token);
//                setData(DATA_SPOTIFY_TOKEN, token);
                this.loggedIn = true;
                saveToken(token);

            });

        });

        return Optional.ofNullable(token).map(t -> "Authorization complete, you can go back to now playing settings").orElse("Authorization failed");
    }

    /**
     * Loads the token from the data folder
     *
     * @return
     */
    private SpotifyToken loadToken() {
        try {
            Path tokenFile = getDataFolder().resolve("token.json");
            String json = Files.readAllLines(tokenFile).stream().collect(Collectors.joining(","));
            SpotifyToken spotifyToken = gson.fromJson(json, SpotifyToken.class);
            loggedIn = true;
            return spotifyToken;
        } catch (IOException e) {
            logger.error("Couldn't load token, maybe it doesn't exist");
            return null;
        }
    }

    /**
     * Saves token to file
     *
     * @param token
     */
    private void saveToken(SpotifyToken token) {
        try {
            Path tokenFile = getDataFolder().resolve("token.json");

            Files.deleteIfExists(tokenFile);

            Files.write(tokenFile, gson.toJson(token).getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Gets a token after the user has authorized the application
     *
     * @param code the code returned by Spotify
     * @param url  the redirect uri that we used
     * @return
     */
    private Optional<SpotifyToken> getToken(String code, String url) {
        try {
            logger.info("Getting token from code [{}] and state [{}]", code, url);
            MultipartBody post = Unirest.post(SPOTIFY_API_TOKEN).header(HttpHeaders.AUTHORIZATION, "Basic " + getBase64Authorization())
                    .field("grant_type", "authorization_code")
                    .field("code", code)
                    .field("redirect_uri", url);


            String body = post.asString().getBody();
            logger.info("Getting token response with body [{}]", body);

            return Optional.ofNullable(gson.fromJson(body, SpotifyToken.class));
        } catch (UnirestException e) {
            logger.error("Couldn't get Spotify token", e);
            return Optional.empty();
        }
    }

    /**
     * Refreshes a token
     */
    private void refreshToken() {
        Optional.ofNullable(token)
                .map(t -> t.refresh_token)
                .ifPresent(r -> {
                    MultipartBody post = Unirest.post(SPOTIFY_API_TOKEN).header(HttpHeaders.AUTHORIZATION, "Basic " + getBase64Authorization())
                            .field("grant_type", "refresh_token")
                            .field("refresh_token", r);


                    String body = null;
                    try {
                        body = post.asString().getBody();
                        logger.info("Getting token response with body [{}]", body);
                        Optional.ofNullable(gson.fromJson(body, SpotifyToken.class)).ifPresent(t -> {
                            token.access_token = t.access_token;
                            loggedIn = true;
                            saveToken(token);

                        });
                    } catch (UnirestException e) {
                        logger.error("couldn't get token via refresh token", e);
                    }

                });
    }

    public String getBase64Authorization() {
        logger.info("Client id [{}], client secret [{}]", clientID, clientSecret);
        return Base64.encodeBase64String((clientID + ":" + clientSecret).getBytes());
    }


    private SpotifyNowPlaying getNowPlaying() throws UnirestException {

        if (token != null) {
            SpotifyNowPlaying nowPlaying = nowPlaying();
            Optional<Error> error = Optional.ofNullable(nowPlaying().error);
            if (!error.isPresent()) {
                loggedIn = true;
                return nowPlaying;
            } else {
                if (error.get().message.equals(MESSAGE_TOKEN_EXPIRED)) {
                    refreshToken();
                    return getNowPlaying();
                } else {
                    loggedIn = false;
                    return null;
                }
            }
        } else {
            loggedIn = false;
            return null;
        }
    }


    /**
     * Gets Spotify currently playing
     *
     * @return
     * @throws UnirestException
     */
    private SpotifyNowPlaying nowPlaying() throws UnirestException {

        HttpResponse<String> stringHttpResponse = Unirest.get(SPOTIFY_CURRENTLY_PLAYING)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.access_token)
                .asString();

//        stringHttpResponse.getHeaders().forEach((s, strings) -> logger.info("Spotify Header {} => {}", s, strings.stream().collect(Collectors.joining(","))));

        String body = stringHttpResponse.getBody();

//        logger.info("Spotify now playing: {}", body);

        SpotifyNowPlaying spotifyNowPlaying = Optional.ofNullable(gson.fromJson(body, SpotifyNowPlaying.class)).orElse(new SpotifyNowPlaying());

        return spotifyNowPlaying;
    }

    @Override
    public String getLoginLinkHtml() {

        if (loggedIn) {
            return "You're already logged in to spotify";
        } else {
            String redirectUri = "location.origin+'/external/" + getId() + "/authorize'";

            String url = "'https://accounts.spotify.com/authorize";
            url += "?client_id=" + clientID;
            url += "&response_type=code";
            url += "&state='+encodeURIComponent(" + redirectUri + ")+'";
            url += "&scope=user-read-playback-state";
            url += "&redirect_uri='+encodeURIComponent(" + redirectUri + ")";
            return "<a  onClick=\"prompt('You need to set the spotifiy application callback URL as below'," + redirectUri + "); window.location.href=" + url + "; return false;\">login to Spotify</a>";
        }
    }
}
