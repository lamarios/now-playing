package com.ftpix.nowplaying.activities;

import com.ftpix.nowplaying.Plugin;
import com.ftpix.nowplaying.Setting;

import java.util.List;
import java.util.Map;

public interface MediaActivityPlugin extends Plugin {

    /**
     * Get the list of all available activities
     *
     * @return
     */
    List<Activity> getActivities() throws Exception;


    /**
     * Get the current activity
     *
     * @return
     */
    Activity getCurrentActivity() throws Exception;


}
