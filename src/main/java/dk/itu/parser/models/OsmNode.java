package dk.itu.parser.models;

public class OsmNode extends OsmElement {

    private final double lat, lon;

    public OsmNode(long id, double lat, double lon) {
        super(id);
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
