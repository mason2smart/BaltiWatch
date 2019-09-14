package com.appcentricity.baltiwatch;

public class ReportItem {
    private String type;
    private double longitude;
    private double latitude;

    public ReportItem(String type, double longitude, double latitude) {
        this.type = type;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public ReportItem(){}

    public String getType() {
        return type;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }


}
