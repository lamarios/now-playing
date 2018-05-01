package com.ftpix.nowplaying.controllers;


import com.ftpix.nowplaying.NowPlayingPlugin;
import com.ftpix.nowplaying.WebApp;
import com.ftpix.nowplaying.activities.Activity;
import com.ftpix.nowplaying.activities.MediaActivityPlugin;
import com.ftpix.nowplaying.utils.PluginUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@WebSocket
public class NowPlayingWebSocket {
    private static final String REFRESH_MESSAGE = "refresh";
    private final int CONTENT_CHECK_DELAY = 30000;
    private final Logger logger = LogManager.getLogger();
    private final static List<Session> users = new ArrayList<>();
    private Object nowPlaying;
    private Timer timer;

    @OnWebSocketConnect
    public void onConnect(Session user) {
        logger.info("New Websocket connection");
        users.add(user);

        try {
            if (timer == null) {
                logger.info("Starting timer");
                timer = new Timer("now-playing");
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        timerTask();
                    }
                }, 0, CONTENT_CHECK_DELAY);
            } else {
                logger.info("Timer already running, letting client know to just refresh image");
                user.getRemote().sendString(REFRESH_MESSAGE);
            }
        } catch (Exception e) {
            logger.error("Couldn't start timer ", e);
        }

    }


    @OnWebSocketClose
    public void onClose(Session user, int statusCpde, String reason) {
        logger.info("Connection is over");

        users.remove(user);

        logger.info("{} clients remaining", users.size());
        // resetting everything
        if (users.isEmpty()) {
            timer.cancel();
            timer = null;
            nowPlaying = null;
            logger.info("Terminating timer");
        }
    }


    public void timerTask() {
        logger.info("Checking content");
        try {
            MediaActivityPlugin mediaActivityPlugin = (MediaActivityPlugin) PluginUtil.PLUGIN_INSTANCES.get(WebApp.CONFIG.selectedActivtyPlugin);
            Activity currentActivity = null;
            currentActivity = mediaActivityPlugin.getCurrentActivity();

            NowPlayingPlugin nowPlayingPlugin = (NowPlayingPlugin) PluginUtil.PLUGIN_INSTANCES.get(WebApp.CONFIG.activityMapping.get(currentActivity.getId()));

            Object nowPlayingContent = nowPlayingPlugin.getNowPlayingContent();
            if (!nowPlayingContent.equals(nowPlaying)) {
                logger.info("Content has change letting clients know");
                nowPlaying = nowPlayingContent;
                users.stream()
                        .map(Session::getRemote)
                        .forEach(u -> {
                            try {
                                u.sendString(REFRESH_MESSAGE);
                            } catch (IOException e) {
                                logger.error("Couldn't send message to client {}", u.getInetSocketAddress().toString(), e);
                            }
                        });
            } else {
                logger.info("Content hasn't change, no need to do anything");
            }

        } catch (Exception e) {
            logger.error("Couldn't get current content", e);

        }
    }


}
