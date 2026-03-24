package dk.itu.renderer.osm;

import java.awt.Color;
import java.awt.Graphics2D;

public class OsmNode extends OsmElement {

    private final float lat, lon;

    public OsmNode(long id, float lat, float lon) {
        super(id);
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
