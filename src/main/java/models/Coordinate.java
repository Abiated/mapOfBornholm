package models;

import java.io.Serializable;

public class Coordinate implements Serializable {
    private static final long serialVersionUID = 1L;

    private final float lat;
    private final float lon;

    public Coordinate(float lat, float lon) {
        this.lat = lat;
        this.lon = lon;
    }

    //Exist for deserializable as it needs a no argument constructor, value does not matter
    private Coordinate() {
        this.lat = 0;
        this.lon = 0;
    };

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }
}