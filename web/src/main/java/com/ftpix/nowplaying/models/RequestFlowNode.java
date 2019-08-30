package com.ftpix.nowplaying.models;

import java.util.List;

public class RequestFlowNode {
    private String name, type;
    private List<RequestFlowNodeOption> options;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<RequestFlowNodeOption> getOptions() {
        return options;
    }

    public void setOptions(List<RequestFlowNodeOption> options) {
        this.options = options;
    }


    @Override
    public String toString() {
        return "RequestFlowNode{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", options=" + options +
                '}';
    }
}
