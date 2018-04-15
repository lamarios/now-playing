package com.ftpix.nowplaying;

import com.ftpix.nowplaying.controllers.ActivitiesController;
import com.ftpix.nowplaying.models.Config;
import com.ftpix.nowplaying.transformers.GsonTransformer;
import com.ftpix.sparknnotation.Sparknotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.HaltException;
import spark.Spark;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WebApp {
    private final static Logger logger = LogManager.getLogger();
    public final static Config CONFIG;

    static {
        Config config;
        if (Files.notExists(Config.CONFIG_FOLDER)) {
            logger.error("Config folder doesn't exist");
            config = null;
        }


        Path configFile = Config.CONFIG_FOLDER.resolve(Config.CONFIG_NAME);
        if (Files.exists(configFile)) {

            try {
                byte[] bytes = Files.readAllBytes(configFile);
                config = GsonTransformer.GSON.fromJson(new String(bytes), Config.class);

            } catch (IOException e) {
                logger.error("Couldn't read config", e);
                config = null;
            }

        } else {
            config = new Config();
        }

        CONFIG = config;

        if(config == null){
           System.exit(-1);
        }

        try {
            config.save();
        } catch (IOException e) {
            logger.error("Couldn't save config",e );
            System.exit(-1);
        }


    }


    public static void main(String[] args) throws IOException {


        if (System.getProperty("dev", "false").equalsIgnoreCase("true")) {
            logger.info("DEV MODE");
            Spark.externalStaticFileLocation("/home/gz/IdeaProjects/now-playing/web/src/main/resources/web/public");

        } else {
            Spark.staticFiles.location("/web/public");
        }



        Spark.before("*", (request, response) -> {
            logger.info("{} {}", request.requestMethod(), request.pathInfo());
        });

        Spark.exception(Exception.class, (e, req, res) -> {
            if (e instanceof InvocationTargetException) {
                InvocationTargetException target = (InvocationTargetException) e;
                if (target.getTargetException() instanceof HaltException) {
                    HaltException halt = (HaltException) target.getTargetException();
                    res.body(halt.body());
                    res.status(halt.statusCode());
                } else {
                    e.printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        });



        Sparknotation.init(GsonTransformer.GSON::fromJson);
    }
}
