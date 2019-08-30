package com.ftpix.nowplaying;

import com.ftpix.nowplaying.controllers.ActivitiesController;
import com.ftpix.nowplaying.controllers.NowPlayingWebSocket;
import com.ftpix.nowplaying.models.Config;
import com.ftpix.nowplaying.transformers.GsonTransformer;
import com.ftpix.nowplaying.utils.PluginUtil;
import com.ftpix.sparknnotation.Sparknotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.HaltException;
import spark.ResponseTransformer;
import spark.Spark;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class WebApp {
    private final static Logger logger = LogManager.getLogger();
    public final static Config CONFIG;
    private static final String PATH_PREFIX = "/external/%s%s";

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

        if (config == null) {
            System.exit(-1);
        }

        try {
            config.save();
        } catch (IOException e) {
            logger.error("Couldn't save config", e);
            System.exit(-1);
        }


    }


    public static void main(String[] args) throws IOException {


        if (System.getProperty("dev", "false").equalsIgnoreCase("true")) {
            logger.info("DEV MODE");
            Spark.externalStaticFileLocation("web/src/main/resources/web/public");

        } else {
            Spark.staticFiles.location("/web/public");
        }
        Spark.webSocket("/ws", NowPlayingWebSocket.class);

        Spark.before("*", (request, response) -> {
            logger.info("{} {}", request.requestMethod(), request.pathInfo());
        });



        PluginUtil.PLUGIN_INSTANCES.values()
                .stream()
                .filter(p -> p instanceof ExternalLoginPlugin)
                .map(p -> (ExternalLoginPlugin) p)
                .filter(p -> Optional.ofNullable(p.defineExternalEndPoints()).isPresent())
                .filter(p -> !p.defineExternalEndPoints().isEmpty())
                .forEach(WebApp::definePluginEndpoints);


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


    public static void definePluginEndpoints(ExternalLoginPlugin p) {
        logger.info("Defining external URLs");

        p.defineExternalEndPoints()
                .stream()
                .filter(e -> e.getMethod() != null && e.getRoute() != null && e.getUrl() != null && e.getUrl().startsWith("/"))
                .forEach(e -> {
                    String url = String.format(PATH_PREFIX, p.getId(), e.getUrl());
                    Optional<ResponseTransformer> transformer = Optional.ofNullable(e.getTransformer());
                    Optional<String> acceptType = Optional.ofNullable(e.getAcceptType());

                    logger.info("Defining url [{} {}] for plugin {}", e.getMethod(), url, p.getName());
                    switch (e.getMethod()) {
                        case GET:
                            if (transformer.isPresent() && acceptType.isPresent()) {
                                Spark.get(url, acceptType.get(), e.getRoute(), transformer.get());
                            } else if (transformer.isPresent() && !acceptType.isPresent()) {
                                Spark.get(url, e.getRoute(), transformer.get());
                            } else if (!transformer.isPresent() && acceptType.isPresent()) {
                                Spark.get(url, acceptType.get(), e.getRoute());
                            } else {
                                Spark.get(url, e.getRoute());
                            }
                            break;
                        case POST:
                            if (transformer.isPresent() && acceptType.isPresent()) {
                                Spark.post(url, acceptType.get(), e.getRoute(), transformer.get());
                            } else if (transformer.isPresent() && !acceptType.isPresent()) {
                                Spark.post(url, e.getRoute(), transformer.get());
                            } else if (!transformer.isPresent() && acceptType.isPresent()) {
                                Spark.post(url, acceptType.get(), e.getRoute());
                            } else {
                                Spark.post(url, e.getRoute());
                            }
                            break;
                        default:

                    }

                });

    }
}
