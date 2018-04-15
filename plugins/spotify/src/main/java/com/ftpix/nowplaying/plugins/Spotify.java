package com.ftpix.nowplaying.plugins;

import com.ftpix.nowplaying.NowPlayingPlugin;
import com.ftpix.nowplaying.Setting;
import com.ftpix.nowplaying.SettingType;
import com.ftpix.nowplaying.activities.Activity;
import com.ftpix.nowplaying.activities.MediaActivityPlugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Spotify implements NowPlayingPlugin, MediaActivityPlugin {

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

    }

    @Override
    public List<String> validateSettings(Map<String, String> settings) {
        return null;
    }

    @Override
    public List<Setting> getSettings() {

        Setting clientId= new Setting();
        clientId.setType(SettingType.TEXT);
        clientId.setName("clientId");
        clientId.setLabel("Client Id");


        Setting clientSecret= new Setting();
        clientSecret.setType(SettingType.TEXT);
        clientSecret.setName("clientSecret");
        clientSecret.setLabel("Client Secret");


        return List.of(clientId, clientSecret);
    }
}
