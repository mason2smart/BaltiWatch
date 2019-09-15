package com.appcentricity.baltiwatch;

import com.google.firebase.firestore.GeoPoint;

public class ReportItem {
    private String Type;
    private GeoPoint Location;
    private String Id;

    public String getId() {
        return Id;
    }

    public ReportItem(String type, GeoPoint Location, String Id) {
        this.Type = type;
        this.Location = Location;
        this.Id = Id;
    }

    public ReportItem(){}

    public String getType() {
        return Type;
    }

    public GeoPoint getLocation() {
        return Location;
    }

}
