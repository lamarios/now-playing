package com.ftpix.nowplaying.plugins.gtsports;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class DailyRace {


    private String trackName, time, imageUrl, carClass, duration;
    private int laps, cars;

    private RaceType raceType;

    public RaceType getRaceType() {
        return raceType;
    }

    public void setRaceType(RaceType raceType) {
        this.raceType = raceType;
        getTimeFromOffset();
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getLaps() {
        return laps;
    }

    public void setLaps(int laps) {
        this.laps = laps;
    }

    public int getCars() {
        return cars;
    }

    public void setCars(int cars) {
        this.cars = cars;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCarClass() {
        return carClass;
    }

    public void setCarClass(String carClass) {
        this.carClass = carClass;
    }

    @Override
    public String toString() {
        return "DailyRace{" +
                "trackName='" + trackName + '\'' +
                ", time='" + time + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", carClass='" + carClass + '\'' +
                ", laps=" + laps +
                ", cars=" + cars +
                ", duration=" + duration +
                ", raceType=" + raceType +
                '}';
    }


    /**
     * Calculate the upcoming time of the race
     *
     * @return the time hh:mm
     */
    private void getTimeFromOffset() {

        LocalDateTime now = LocalDateTime.now();
        int startFrom = 0 + raceType.getOffset();

        LocalDateTime time = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), startFrom);
        while (time.isBefore(now)) {
            time = time.plusMinutes(raceType.getDuration());
        }

        this.time = time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyRace dailyRace = (DailyRace) o;
        return laps == dailyRace.laps &&
                cars == dailyRace.cars &&
                Objects.equals(trackName, dailyRace.trackName) &&
                Objects.equals(time, dailyRace.time) &&
                Objects.equals(imageUrl, dailyRace.imageUrl) &&
                Objects.equals(carClass, dailyRace.carClass) &&
                Objects.equals(duration, dailyRace.duration) &&
                raceType == dailyRace.raceType;
    }

    @Override
    public int hashCode() {

        return Objects.hash(trackName, time, imageUrl, carClass, duration, laps, cars, raceType);
    }
}
