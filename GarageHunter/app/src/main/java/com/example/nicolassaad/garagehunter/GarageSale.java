package com.example.nicolassaad.garagehunter;

/**
 * Created by nicolassaad on 5/6/16.
 */
public class GarageSale {
    private String title, description, address, weekday;
    private int lat, lon;

    public GarageSale(String title, String description, String address, String weekday) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.weekday = weekday;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public int getLat() {
        return lat;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

    public int getLon() {
        return lon;
    }

    public void setLon(int lon) {
        this.lon = lon;
    }
}