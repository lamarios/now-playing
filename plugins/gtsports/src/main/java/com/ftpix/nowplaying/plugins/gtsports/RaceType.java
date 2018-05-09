package com.ftpix.nowplaying.plugins.gtsports;

public enum RaceType {
    RACE_A("Race A", 5, 20),
    RACE_B("Race B", 0, 20),
    RACE_C("Race C", 0, 30);


    RaceType(String label, int offset, int duration) {
        this.label = label;
        this.offset = offset;
        this.duration = duration;
    }

    private final String label;
    private final int offset, duration;

    public String getLabel() {
        return label;
    }

    public int getOffset() {
        return offset;
    }

    public int getDuration() {
        return duration;
    }
}
