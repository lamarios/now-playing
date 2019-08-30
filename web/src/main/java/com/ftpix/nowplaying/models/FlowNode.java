package com.ftpix.nowplaying.models;

import spark.Request;

import java.util.Map;

public class FlowNode {
    private ActivityNode activity;
    private String nowPlaying;
    private RequestFlowNode request;

    public String getNowPlaying() {
        return nowPlaying;
    }

    public void setNowPlaying(String nowPlaying) {
        this.nowPlaying = nowPlaying;
    }

    public ActivityNode getActivity() {
        return activity;
    }

    public void setActivity(ActivityNode activity) {
        this.activity = activity;
    }

    public RequestFlowNode getRequest() {
        return request;
    }

    public void setRequest(RequestFlowNode request) {
        this.request = request;
    }


    @Override
    public String toString() {
        return "FlowNode{" +
                "activity=" + activity +
                ", nowPlaying='" + nowPlaying + '\'' +
                ", request=" + request +
                '}';
    }
}
