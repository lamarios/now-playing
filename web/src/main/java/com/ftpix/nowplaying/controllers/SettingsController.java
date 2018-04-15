package com.ftpix.nowplaying.controllers;

import com.ftpix.nowplaying.Plugin;
import com.ftpix.nowplaying.WebApp;
import com.ftpix.nowplaying.models.Config;
import com.ftpix.nowplaying.transformers.GsonTransformer;
import com.ftpix.nowplaying.utils.PluginUtil;
import com.ftpix.sparknnotation.annotations.SparkController;
import com.ftpix.sparknnotation.annotations.SparkPost;
import com.ftpix.sparknnotation.annotations.SparkQueryParam;
import spark.Request;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;

@SparkController("/api/settings")
public class SettingsController {


    @SparkPost(value = "/save", transformer = GsonTransformer.class)
    public List<String> savePluginSetting(@SparkQueryParam("pluginId") String plugin, Request request) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
        List<String> errors = new ArrayList<>();
        if (plugin != null && plugin.trim().length() > 0) {

            Map<String, String> settings = new HashMap<>();
            request.queryMap().toMap().forEach((k, v) -> {
                if (!k.equalsIgnoreCase("pluginId")) {
                    settings.put(k, v[0]);
                }
            });

            Plugin instance = PluginUtil.PLUGIN_INSTANCES.get(plugin);
            errors = instance.validateSettings(settings);

            if(errors == null){
                errors = Collections.emptyList();
            }

            if (errors.isEmpty()) {
                WebApp.CONFIG.pluginSettings.put(plugin, settings);
                WebApp.CONFIG.save();

                instance.stop();
                instance.init(settings);
            }


        } else {
            errors.add("No plugin provided");
        }


        return errors;
    }

}
