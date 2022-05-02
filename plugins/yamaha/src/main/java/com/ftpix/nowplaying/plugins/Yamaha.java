package com.ftpix.nowplaying.plugins;

import com.ftpix.nowplaying.NowPlayingPlugin;
import com.ftpix.nowplaying.Setting;
import com.ftpix.nowplaying.SettingType;
import com.ftpix.nowplaying.activities.Activity;
import com.ftpix.nowplaying.activities.MediaActivityPlugin;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Yamaha implements MediaActivityPlugin {
    private static final String SETTING_HOST = "host";
    private final String PATH = "/YamahaRemoteControl/ctrl";
    private final Logger logger = LogManager.getLogger();
    private String host;


    @Override
    public String getName() {
        return "Yamaha Amplifier";
    }

    @Override
    public String getId() {
        return "com.ftpix.nowplaying.plugins.yamaha";
    }

    @Override
    public void init(Map<String, String> settings) {
        host = settings.get(SETTING_HOST);

    }

    @Override
    public List<String> validateSettings(Map<String, String> settings) {

        YNCRequest request = YNC.COMMANDS.get(YNC.GET_POWER_STATUS);
        try {
            String body = Unirest.post("http://" + settings.get(SETTING_HOST) + PATH)
                    .body(request.getRequest())
                    .asString()
                    .getBody();
            String on = request.getResponseValue(body.replaceAll("[\n\r]", ""));
            logger.info("Amp response: {}", on);
            return null;
        } catch (Exception e) {
            logger.error("Couldn't reach amp", e);
            return List.of("The amplifier can't be reached " + e.getMessage());
        }

    }

    @Override
    public List<Setting> getSettings() {
        Setting host = new Setting();
        host.setName(SETTING_HOST);
        host.setType(SettingType.TEXT);

        host.setLabel("Amplifier Host/IP");

        return List.of(host);
    }

    @Override
    public void stop() {

    }

    @Override
    public List<Activity> getActivities() throws Exception {
        List<Activity> activities = new ArrayList<>();

        Activity standBy = new Activity();
        standBy.setName("Stand By");
        standBy.setId("Standby");
        activities.add(standBy);

        Activity audio = new Activity();
        audio.setName("Audio");
        audio.setId("AUDIO");
        activities.add(audio);

        YNCRequest request = YNC.COMMANDS.get(YNC.GET_CONFIG);

        String body = Unirest.post("http://" + host + PATH)
                .body(request.getRequest())
                .asString()
                .getBody();
        //parsing the document
        DocumentBuilderFactory dbf =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(body));
        Document doc = db.parse(is);

        NodeList input = doc.getElementsByTagName("Input");
        if (input.getLength() == 1) {
            NodeList inputs = input.item(0).getChildNodes();
            for (int i = 0; i < inputs.getLength(); i++) {
                Node item = inputs.item(i);

                Activity a = new Activity();
                a.setName(item.getTextContent());
                a.setId(item.getNodeName().replaceAll("[^a-zA-Z0-9]", ""));

                activities.add(a);

            }
        }

        NodeList features = doc.getElementsByTagName("Feature_Existence");
        if (features.getLength() == 1) {
            NodeList inputs = features.item(0).getChildNodes();
            for (int i = 0; i < inputs.getLength(); i++) {
                Node item = inputs.item(i);
                String nodeName = item.getNodeName();
                String content = item.getTextContent();
                if (item.getTextContent().equalsIgnoreCase("1")) {
                    Activity a = new Activity();
                    a.setName(nodeName);
                    a.setId(nodeName);
                    activities.add(a);
                }
            }
        }

        return activities;
    }

    @Override
    public Activity getCurrentActivity() throws Exception {


        YNCRequest request = YNC.COMMANDS.get(YNC.GET_POWER_STATUS);
        String body = Unirest.post("http://" + host + PATH)
                .body(request.getRequest())
                .asString()
                .getBody();
        String on = request.getResponseValue(body.replaceAll("[\n\r]", ""));
        if (on.equalsIgnoreCase("Standby")) {

            Activity standBy = new Activity();
            standBy.setName("Stand By");
            standBy.setId("Standby");
            return standBy;
        } else {

            request = YNC.COMMANDS.get(YNC.GET_INPUT);
            body = Unirest.post("http://" + host + PATH)
                    .body(request.getRequest())
                    .asString()
                    .getBody();

            String responseValue = request.getResponseValue(body.replaceAll("[\n\r]", ""));
            Activity a = new Activity();
            a.setId(responseValue);
            a.setName(responseValue);

            logger.info("Yamaha current activity : {}", a.getId());
            return a;
        }
    }


}
