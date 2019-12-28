package com.joecompany.pinit.data;

import java.util.UUID;

public class PinData {

    public static final String PIN_FIELD_NAME = "name";
    public static final String PIN_FIELD_LATITUDE = "latitude";
    public static final String PIN_FIELD_LONGITUDE = "longitude";
    public static final String PIN_FIELD_ID = "id";

    public double latitude;
    public double longitude;
    public String name;
    public String id;


    public PinData(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = UUID.randomUUID().toString();
    }
}