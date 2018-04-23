package com.ftpix.nowplaying.plugins.models;

public class SpotifyNowPlaying {

    public long progress_ms = -1, timestamp;
    public boolean is_playing;
    public Item item;
    public Error error;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SpotifyNowPlaying) {
            SpotifyNowPlaying o = (SpotifyNowPlaying) obj;
            if (item != null && o.item != null) {
                return item.id.equalsIgnoreCase(o.item.id);
            }
        }
        return false;
    }
}
