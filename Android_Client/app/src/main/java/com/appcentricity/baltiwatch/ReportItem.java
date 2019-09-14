package com.appcentricity.baltiwatch;

import com.google.firebase.firestore.GeoPoint;

public class ReportItem {
    private String Type;
    private GeoPoint Location;

    public ReportItem(String type, GeoPoint Location) {
        this.Type = type;
        this.Location = Location;
    }

    public ReportItem(){}

    public String getType() {
        return Type;
    }

    public GeoPoint getLocation() {
        return Location;
    }

}
