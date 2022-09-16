package com.ftpix.nowplaying.models;

import com.ftpix.nowplaying.TemplateVariable;

import java.util.HashMap;
import java.util.Map;

public class CustomScreen {
    private int width, height;

    private String id;
    private HashMap<String, HashMap<String, TemplateVariable>> pluginTemplates = new HashMap<>();

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, HashMap<String, TemplateVariable>> getPluginTemplates() {
        return pluginTemplates;
    }

    public void setPluginTemplates(HashMap<String, HashMap<String, TemplateVariable>> pluginTemplates) {
        this.pluginTemplates = pluginTemplates;
    }
}
