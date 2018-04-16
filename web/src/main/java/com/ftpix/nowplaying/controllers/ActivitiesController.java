package com.ftpix.nowplaying.controllers;


import com.ftpix.nowplaying.Plugin;
import com.ftpix.nowplaying.activities.Activity;
import com.ftpix.nowplaying.activities.MediaActivityPlugin;
import com.ftpix.nowplaying.plugins.Blackscreen;
import com.ftpix.nowplaying.transformers.GsonTransformer;
import com.ftpix.nowplaying.utils.PluginUtil;
import com.ftpix.sparknnotation.annotations.SparkController;
import com.ftpix.sparknnotation.annotations.SparkGet;
import com.ftpix.sparknnotation.annotations.SparkPost;
import com.ftpix.sparknnotation.annotations.SparkQueryParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ftpix.nowplaying.WebApp.CONFIG;

@SparkController("/api/activities")
public class ActivitiesController {

    private final Logger logger = LogManager.getLogger();

    /**
     * Gets the list of available controllers
     *
     * @return
     * @throws IOException
     */
    @SparkGet(value = "/get-available-plugins", transformer = GsonTransformer.class)
    public List<Map<String,Object>> getAvailablePlugins() throws IOException {

        return PluginUtil.PLUGIN_INSTANCES
                .values()
                .stream()
                .filter(p -> p != null)
                .filter(p -> p instanceof MediaActivityPlugin)
                .map(PluginUtil.PLUGIN_TO_ID_NAME)
                .collect(Collectors.toList());
    }

    /**
     * Gets the activities of the currently selected activity plugin
     *
     * @return
     * @throws Exception
     */
    @SparkGet(value = "/get-activities", transformer = GsonTransformer.class)
    public List<Activity> getCurrentPluginActivities() throws Exception {
        return ((MediaActivityPlugin) PluginUtil.PLUGIN_INSTANCES.get(CONFIG.selectedActivtyPlugin)).getActivities();
    }

    /**
     * Get the currently active plugin
     *
     * @return
     */
    @SparkGet(value = "/get-current-plugin")
    public String getCurrent() {
        return CONFIG.selectedActivtyPlugin;
    }


    /**
     * Get the set up  activity / now playing
     *
     * @return
     */
    @SparkGet(value = "/get-mapping", transformer = GsonTransformer.class)
    public Map<String, String> getActivityMapping() {
        return CONFIG.activityMapping;
    }


    /**
     * Save Mapping
     */
    @SparkPost("/set-mapping")
    public boolean saveMapping(@SparkQueryParam("activity") String activity, @SparkQueryParam("pluginId") String pluginId) throws IOException {
        if (PluginUtil.pluginExists(pluginId) && activity != null && activity.trim().length() > 0) {
            CONFIG.activityMapping.put(activity, pluginId);
            CONFIG.save();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Saves the currently active Activity plugin
     *
     * @param current
     * @return
     * @throws IOException
     */
    @SparkPost(value = "/set")
    public boolean setCurrent(@SparkQueryParam("plugin") String current) throws Exception {
        if (PluginUtil.pluginExists(current)) {
            CONFIG.selectedActivtyPlugin = current;
            CONFIG.activityMapping = new HashMap<>();

            ((MediaActivityPlugin) PluginUtil.PLUGIN_INSTANCES.get(current))
                    .getActivities()
                    .forEach(a -> {
                        CONFIG.activityMapping.put(a.getId(), Blackscreen.BLACK_SCREEN_PLUGIN_ID);
                    });

            CONFIG.save();

            return true;
        } else {
            return false;
        }
    }


}
