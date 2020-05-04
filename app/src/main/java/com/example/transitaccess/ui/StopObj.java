package com.example.transitaccess.ui;

public class StopObj {
    private String stopId;
    private String stopName;
    private String lat;
    private String lng;

    public StopObj(String stopId, String stopName, String lat, String lng) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.lat = lat;
        this.lng = lng;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
