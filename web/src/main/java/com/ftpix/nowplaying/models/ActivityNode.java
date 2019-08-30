package com.ftpix.nowplaying.models;

import java.util.Map;

public class ActivityNode {
    private String plugin;
    private Map<String, FlowNode> nodes;

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public Map<String, FlowNode> getNodes() {
        return nodes;
    }

    public void setNodes(Map<String, FlowNode> nodes) {
        this.nodes = nodes;
    }


    @Override
    public String toString() {
        return "ActivityNode{" +
                "plugin='" + plugin + '\'' +
                ", nodes=" + nodes +
                '}';
    }
}
