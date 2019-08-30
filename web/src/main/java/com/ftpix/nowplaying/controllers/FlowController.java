package com.ftpix.nowplaying.controllers;

import com.ftpix.nowplaying.WebApp;
import com.ftpix.nowplaying.models.Config;
import com.ftpix.nowplaying.models.FlowNode;
import com.ftpix.nowplaying.transformers.GsonBodyTransformer;
import com.ftpix.nowplaying.transformers.GsonTransformer;
import com.ftpix.sparknnotation.annotations.SparkBody;
import com.ftpix.sparknnotation.annotations.SparkController;
import com.ftpix.sparknnotation.annotations.SparkGet;
import com.ftpix.sparknnotation.annotations.SparkPost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static com.ftpix.nowplaying.WebApp.CONFIG;

@SparkController("/api/flow")
public class FlowController {
    private final Logger logger = LogManager.getLogger();


    @SparkPost(accept = "application/json", transformer = GsonTransformer.class)
    public boolean saveFLow(@SparkBody(GsonBodyTransformer.class) FlowNode flow) throws IOException {
        CONFIG.flow = flow;
        CONFIG.save();

        return true;
    }


    @SparkGet(transformer = GsonTransformer.class)
    public FlowNode getFlow() {
        return CONFIG.flow;
    }
}
