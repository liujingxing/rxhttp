package com.example.httpsender.entity;

/**
 * User: ljx
 * Date: 2019-11-18
 * Time: 14:51
 */
public class Location {

    private double longitude;
    private double latitude;

    public Location(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
