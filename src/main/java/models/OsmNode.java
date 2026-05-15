package models;

import java.io.Serial;
import java.io.Serializable;

public class OsmNode extends OsmElement implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final float lat, lon;

    public OsmNode(long id, float lat, float lon) {
        super(id);
        this.lat = lat;
        this.lon = lon;
    }

    //Exist for deserializable as it needs a no argument constructor, value does not matter
    private OsmNode() {
        super(0);
        this.lat = 0;
        this.lon = 0;
    }

    @Override
    public float[] getBoundingBox() {
        return new float[]{lon, lon, lat, lat};
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }



}