package com.ftpix.nowplaying.controllers;


import com.ftpix.nowplaying.*;
import com.ftpix.nowplaying.models.CustomScreen;
import com.ftpix.nowplaying.transformers.GsonTransformer;
import com.ftpix.nowplaying.utils.PluginUtil;
import com.ftpix.sparknnotation.annotations.*;
import com.google.gson.reflect.TypeToken;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ftpix.nowplaying.WebApp.*;

@SparkController("/api/screens")
public class CustomScreensController {


    @SparkPost(value = "/add", transformer = GsonTransformer.class)
    public void addScreen(@SparkBody CustomScreen screen, Request request, Response response) throws IOException {
        if (screen == null || screen.getWidth() == 0 || screen.getHeight() == 0 || screen.getId() == null || screen.getId().trim().length() == 0) {
            response.status(400);
            return;
        }

        screen.setId(screen.getId().trim());
        if (CONFIG.screens.containsKey(screen.getId())) {
            response.body("Screen screen.getId() already exists");
            response.status(400);
            return;
        }

        CONFIG.screens.put(screen.getId(), screen);
        CONFIG.save();
    }

    @SparkGet(transformer = GsonTransformer.class)
    public Map<String, CustomScreen> getScreens() {
        return CONFIG.screens;
    }

    @SparkGet(value = "/available-plugins", transformer = GsonTransformer.class)
    public List<Map<String, Object>> getCustomizablePlugins() {
        return PluginUtil.PLUGIN_INSTANCES.values().stream()
                .filter(p -> p instanceof WithCustomScreen)
                .map(p -> {
                    WithCustomScreen customScreen = ((WithCustomScreen) p);
                    Map<String, Object> values = new HashMap<>();
                    values.put("id", ((Plugin) p).getId());
                    values.put("name", ((Plugin) p).getName());
                    values.put("variables", customScreen.getTemplateVariables());
                    return values;
                })
                .collect(Collectors.toList());
    }

    @SparkPost(value = "/variables/:screenId/:pluginId")
    public void saveVariables(@SparkParam("screenId") String screenId, @SparkParam("pluginId") String pluginId, Request request, Response response) throws IOException {
        if (!CONFIG.screens.containsKey(screenId)) {
            response.status(404);
            response.body("Screen doesn't exist");
        }

        Type type = new TypeToken<HashMap<String, TemplateVariable>>() {
        }.getType();
        HashMap<String, TemplateVariable> variables = GsonTransformer.GSON.fromJson(request.body(), type);
        variables.forEach((name, values) -> System.out.println(values.getX()));

        CustomScreen customScreen = CONFIG.screens.get(screenId);

        customScreen.getPluginTemplates().put(pluginId, variables);
        CONFIG.save();
    }

}
