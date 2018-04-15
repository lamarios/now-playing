package com.ftpix.nowplaying.harmony;

import com.ftpix.nowplaying.Setting;
import com.ftpix.nowplaying.SettingType;
import com.ftpix.nowplaying.activities.Activity;
import com.ftpix.nowplaying.activities.MediaActivityPlugin;
import net.whistlingfish.harmony.HarmonyClient;
import net.whistlingfish.harmony.protocol.LoginToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HarmonyPlugin implements MediaActivityPlugin {

    private static final String SETTING_IP = "ip";
    private static final String SETTING_USERNAME = "username";
    private static final String SETTING_PASSWORD = "password";
    private Map<String, String> settings;


    public List<Activity> getActivities() {
        List<Activity> activities = new ArrayList<>();
        try {
            HarmonyClient client = connectToClient(settings);
            try {
                activities = client.getConfig().getActivities().stream()
                        .map(a -> {
                            Activity activity = new Activity();
                            activity.setId(a.getId().toString());
                            activity.setName(a.getLabel());
                            return activity;
                        }).collect(Collectors.toList());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                client.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activities;

    }

    public Activity getCurrentActivity() {
        HarmonyClient client = connectToClient(settings);

        try {
            net.whistlingfish.harmony.config.Activity currentActivity = client.getCurrentActivity();
            Activity activity = new Activity();
            activity.setId(Integer.toString(currentActivity.getId()));
            activity.setName(currentActivity.getLabel());

            return activity;
        } finally {
            client.disconnect();
        }
    }

    public void stop() {
        HarmonyClient.getInstance().disconnect();
    }

    public String getName() {
        return "Harmony Hub";
    }

    public String getId() {
        return "com.ftpix.nowplaying.harmony";
    }

    public void init(Map<String, String> settings) {
        this.settings = settings;

    }

    public List<String> validateSettings(Map<String, String> settings) {
        try {

            HarmonyClient client = connectToClient(settings);

            client.getCurrentActivity();

            return Collections.emptyList();
        } catch (Exception e) {
            ArrayList<String> errors = new ArrayList<String>();
            errors.add("Couldn't connect to Hub: " + e.getMessage());

            return errors;
        }

    }

    public List<Setting> getSettings() {
        List<Setting> settings = new ArrayList<Setting>();

        Setting host = new Setting();
        host.setDescription("ip, hostname of the harmony hub");
        host.setLabel("Ip");
        host.setName(SETTING_IP);
        host.setType(SettingType.TEXT);
        settings.add(host);

        Setting username = new Setting();
        username.setDescription("Username/Email used to login to myharmony");
        username.setLabel("Username");
        username.setName(SETTING_USERNAME);
        username.setType(SettingType.TEXT);
        settings.add(username);

        Setting password = new Setting();
        password.setDescription("");
        password.setLabel("Password");
        password.setName(SETTING_PASSWORD);
        password.setType(SettingType.PASSWORD);
        settings.add(password);

        return settings;
    }

    /**
     * Connects to the Hub
     *
     * @param settings the settings to connect
     * @return the connected client
     */
    private HarmonyClient connectToClient(Map<String, String> settings) {

        HarmonyClient client = HarmonyClient.getInstance();

        LoginToken token = new LoginToken(settings.get(SETTING_USERNAME), settings.get(SETTING_PASSWORD));

        client.connect(settings.get(SETTING_IP), token);

        return client;
    }
}
