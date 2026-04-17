package models;

public class Coordinate {

    private final float lat;
    private final float lon;

    public Coordinate(float lat, float lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }
}