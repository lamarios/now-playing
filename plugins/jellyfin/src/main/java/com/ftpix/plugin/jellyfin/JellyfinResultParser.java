package com.ftpix.plugin.jellyfin;

import com.ftpix.plugin.jellyfin.model.NowPlayingItem;
import com.ftpix.plugin.jellyfin.model.JellyfinSession;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JellyfinResultParser {

    public static List<JellyfinSession> parseJson(JSONArray array) {

        Gson gson = new Gson();
        List<JellyfinSession> sessions = new ArrayList<>();
        array.forEach(s -> {
            sessions.add(gson.fromJson(s.toString(), JellyfinSession.class));
        });


        return sessions;


    }
}
