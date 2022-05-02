package com.ftpix.plugin.jellyfin.model;

public class JellyfinSession {
    private NowPlayingItem NowPlayingItem;
    private String DeviceName, UserName;

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

    public NowPlayingItem getNowPlayingItem() {
        return NowPlayingItem;
    }

    public void setNowPlayingItem(NowPlayingItem nowPlayingItem) {
        this.NowPlayingItem = nowPlayingItem;
    }
}
