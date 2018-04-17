package com.ftpix.nowplaying;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public interface Plugin {
    Path CONFIG_FOLDER = Paths.get(System.getProperty("config.folder", "."));
    /**
     * Gets the name of this plugin
     *
     * @return
     */
    String getName();

    /**
     * Gets the id of this plugin
     *
     * @return
     */
    String getId();

    /**
     * What to do when a new instance is created
     *
     * @param settings to apply to this plugin
     */
    void init(Map<String, String> settings);

    /**
     * Validate the settings given to this plugin
     *
     * @param settings the settings with its values
     * @return null or empty map if everything is ok, a list of error messages if anything goes wrong
     */
    List<String> validateSettings(Map<String, String> settings);


    /**
     * Gets a the list of settings to show in the front end
     *
     * @return
     */
    List<Setting> getSettings();

    /**
     * What to do
     * when stopping
     * the activity
     */

    void stop();

    default Path getCacheFolder() throws IOException {
        Path data = CONFIG_FOLDER.resolve(getId()).resolve("cache").toAbsolutePath();
        Files.createDirectories(data);
        return  data;
    }

    default Path getDataFolder() throws IOException {
        Path data = CONFIG_FOLDER.resolve(getId()).resolve("data").toAbsolutePath();
        Files.createDirectories(data);
        return  data;
    }
}
