package com.joecompany.pinit.data;

import java.util.Random;
import java.util.UUID;

public class PinData {

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