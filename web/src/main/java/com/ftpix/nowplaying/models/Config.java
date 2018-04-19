package com.ftpix.nowplaying.models;

import com.ftpix.nowplaying.plugins.Blackscreen;
import com.ftpix.nowplaying.plugins.defaultactivity.DefaultActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final Path CONFIG_FOLDER = Paths.get(System.getProperty("config.folder", "."));
    public static final String CONFIG_NAME = "config.json";

    public String selectedActivtyPlugin = DefaultActivity.DEFAULT_ACTIVITY_PLUGIN_ID;
    public Map<String, String> activityMapping = new HashMap<>();
    public Map<String, Map<String, String>> pluginSettings = new HashMap<>();

    public Config() {
        activityMapping.put(DefaultActivity.DEFAULT_ACTIVITY_NAME, Blackscreen.BLACK_SCREEN_PLUGIN_ID);
    }

    public synchronized void save() throws IOException {

        Path configFile = CONFIG_FOLDER.resolve(CONFIG_NAME);
        Path backup = CONFIG_FOLDER.resolve(CONFIG_NAME + ".back");
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            if (Files.exists(backup)) {
                Files.move(configFile, backup, StandardCopyOption.REPLACE_EXISTING);
            }

            String jsonStr = gson.toJson(this);
            Files.deleteIfExists(configFile);
            Files.write(configFile, jsonStr.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            if (Files.exists(backup)) {
                Files.move(backup, configFile, StandardCopyOption.REPLACE_EXISTING);
            }
            throw e;
        } finally {
            Files.deleteIfExists(backup);
        }

    }
}
