package com.ftpix.nowplaying.models;

public class RequestFlowNodeOption {
    private String name;
    private FlowNode value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FlowNode getValue() {
        return value;
    }

    public void setValue(FlowNode value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return "RequestFlowNodeOption{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
