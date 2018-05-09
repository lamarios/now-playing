package com.ftpix.nowplaying.plugins.gtsports;

import java.util.Objects;

public class GTSportRaces {
    private DailyRace raceA, raceB, raceC;

    public DailyRace getRaceA() {
        return raceA;
    }

    public void setRaceA(DailyRace raceA) {
        this.raceA = raceA;
    }

    public DailyRace getRaceB() {
        return raceB;
    }

    public void setRaceB(DailyRace raceB) {
        this.raceB = raceB;
    }

    public DailyRace getRaceC() {
        return raceC;
    }

    public void setRaceC(DailyRace raceC) {
        this.raceC = raceC;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GTSportRaces that = (GTSportRaces) o;
        return Objects.equals(raceA, that.raceA) &&
                Objects.equals(raceB, that.raceB) &&
                Objects.equals(raceC, that.raceC);
    }

    @Override
    public int hashCode() {

        return Objects.hash(raceA, raceB, raceC);
    }
}
