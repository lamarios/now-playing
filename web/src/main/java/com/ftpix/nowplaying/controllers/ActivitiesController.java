package com.ftpix.nowplaying.controllers;


import com.ftpix.nowplaying.Plugin;
import com.ftpix.nowplaying.activities.Activity;
import com.ftpix.nowplaying.activities.MediaActivityPlugin;
import com.ftpix.nowplaying.plugins.Blackscreen;
import com.ftpix.nowplaying.transformers.GsonTransformer;
import com.ftpix.nowplaying.utils.PluginUtil;
import com.ftpix.sparknnotation.annotations.*;
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
     * Gets the activities of an activity plugin
     *
     * @return
     * @throws Exception
     */
    @SparkGet(value = "/get-activities/:plugin", transformer = GsonTransformer.class)
    public List<Activity> getActivities(@SparkParam("plugin") String pluginId) throws Exception {
        return ((MediaActivityPlugin) PluginUtil.PLUGIN_INSTANCES.get(pluginId)).getActivities();
    }







}
