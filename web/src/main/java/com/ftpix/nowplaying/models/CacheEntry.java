package com.ftpix.nowplaying.models;

import java.time.LocalDateTime;

public class CacheEntry {
    public Object content;
    public LocalDateTime time;
    public byte[] image;

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
