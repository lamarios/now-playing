package com.ftpix.nowplaying.plugins;

import com.ftpix.nowplaying.NowPlayingPlugin;
import com.ftpix.nowplaying.Setting;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Blackscreen implements NowPlayingPlugin {

    public static final String BLACK_SCREEN_PLUGIN_ID = "com.ftpix.nowplaying.plugin.blackscreen";

    @Override
    public void getNowPlayingImage(Graphics2D graphics, Dimension dimension, double scale) {
        graphics.setPaint(Color.BLACK);
        graphics.fillRect(0, 0, dimension.width, dimension.height);
    }

    @Override
    public String getName() {
        return "Black Screen";
    }

    @Override
    public String getId() {
        return BLACK_SCREEN_PLUGIN_ID;
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
        return null;
    }

    @Override
    public void stop() {

    }
}
