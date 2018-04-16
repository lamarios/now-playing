package com.ftpix.nowplaying.plugins;

import com.ftpix.nowplaying.*;
import com.ftpix.nowplaying.activities.Activity;
import com.ftpix.nowplaying.activities.MediaActivityPlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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


    private final Gson gson = new GsonBuilder().create();

    @Override
    public void getNowPlayingImage(Graphics2D graphics, Dimension dimension) {

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
        return null;
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
        logger.info("SETTINGS:{}" , settings);
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
                //TODO save token
            });

        });

        return Optional.ofNullable(token).map(t -> "Authorization complete, you can go back to HomeDash").orElse("Authorization failed");
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
                            //TODO save token

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

    @Override
    public String getLoginLinkHtml() {

        String redirectUri = "location.origin+'/external/"+getId()+"/authorize'";

        String url = "'https://accounts.spotify.com/authorize";
        url += "?client_id=" + clientID;
        url += "&response_type=code";
        url += "&state='+encodeURIComponent("+redirectUri+")+'";
        url += "&scope=user-read-playback-state";
        url += "&redirect_uri='+encodeURIComponent("+redirectUri+")";
        return "<a  onClick=\"prompt('You need to set the spotifiy application callback URL as below',"+redirectUri+"); window.location.href="+url+"; return false;\">login to Spotify</a>";
    }
}
