package com.ftpix.nowplaying.plugins.defaultactivity;

import com.ftpix.nowplaying.Setting;
import com.ftpix.nowplaying.activities.Activity;
import com.ftpix.nowplaying.activities.MediaActivityPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DefaultActivity implements MediaActivityPlugin {

    public static final String DEFAULT_ACTIVITY_PLUGIN_ID = "com.ftpix.nowplaying.plugins.DefaultActivity";
    public static final String DEFAULT_ACTIVITY_NAME = "default";

    @Override
    public List<Activity> getActivities() throws Exception {
        Activity activity = new Activity();
        activity.setName("Default");
        activity.setId("default");
        return List.of(activity);
    }

    @Override
    public Activity getCurrentActivity() throws Exception {
        Activity activity = new Activity();
        activity.setName("Default");
        activity.setId(DEFAULT_ACTIVITY_NAME);
        return activity;
    }

    @Override
    public void stop() {

    }

    @Override
    public String getName() {
        return "Default activity";
    }

    @Override
    public String getId() {
        return DEFAULT_ACTIVITY_PLUGIN_ID;
    }

    @Override
    public void init(Map<String, String> settings) {

    }

    @Override
    public List<String> validateSettings(Map<String, String> settings) {
        return null;
    }

    @Override
    public List<Setting> getSettings() {
        return Collections.emptyList();
    }
}
