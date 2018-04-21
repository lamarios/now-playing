package com.ftpix.plugin.plex;

import com.ftpix.plugin.plex.model.MediaContainer;
import com.ftpix.plugin.plex.model.PlexSession;
import com.mashape.unirest.http.JsonNode;
import org.json.JSONObject;

public class PlexResultParser {

    public static PlexSession parseJson(JsonNode node) {

        PlexSession plexSession = new PlexSession();

        JSONObject mediaContainerJSON = node.getObject().getJSONObject("MediaContainer");

        plexSession.setMediaContainer(MediaContainer.fromJson(mediaContainerJSON));

        return plexSession;


    }
}
