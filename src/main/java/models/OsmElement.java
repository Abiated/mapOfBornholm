package models;

import interfaces.InsertableInRtree;
import rendering.wayType;
import java.io.Serializable;
import java.util.Map;

public abstract class OsmElement implements Serializable, InsertableInRtree {
    private static final long serialVersionUID = 1L;

    private final long id;
    private float area;
    Map<String, String> tags;

    //Constructor for tag elements
    public OsmElement(long id, Map<String, String> tags) {this.id = id; this.tags = tags;}
    //Constructor for simple elements without tags
    public OsmElement(long id) {this.id = id;}
    //Constructor for deserializable
    private OsmElement() {this.id = 0;}

    public long getId() {
        return id;
    }

    public float getArea() {
        return this.area;
    }

    protected void setArea(float area) {
        this.area = area;
    }

    //TAG HANDLING
    public Map<String, String> getTags() {
        return tags;
    }

    public boolean hasTag(String key, String value) {
        if(tags == null) return false;
        return value.equals(tags.get(key));
    }

    @Override
    public abstract float[] getBoundingBox();

    @Override
    public boolean intersects(float[] otherBbox) {
        float[] bb = getBoundingBox();
        return !(bb[1] < otherBbox[0] ||
                bb[0] > otherBbox[1] ||
                bb[3] < otherBbox[2] ||
                bb[2] > otherBbox[3]);
    }


    //Tag checker
    public boolean isType(wayType type) {
        if (this.tags == null) return false;
        switch (type) {
            case wayType.NON_DRAWING:
                return (hasTag("location", "underground")
                        || tags.get("leisure") != null
                        || tags.get("tunnel") != null
                        || hasTag("landuse", "harbour")
                        || hasTag("highway", "proposed"));
            case wayType.ROAD:
                return !hasTag("highway", "proposed") && tags.get("highway") != null;
            case wayType.LANDUSE:
                return tags.get("landuse") != null;
            case wayType.COASTLINE:
                //Place key for relations can be larger complicated polygons that can represent coastlines
                //All are not included here
                return hasTag("natural", "coastline") || hasTag("place", "island") || hasTag("place", "country");
            case wayType.SEAROUTE:
                return hasTag("route", "ferry") || hasTag("seamark:type", "ferry_route");
            case wayType.BUILDING:
                return tags.get("building") != null;
            case wayType.NATURAL:
                return tags.get("natural") != null && !hasTag("natural", "water") && !hasTag("natural", "coastline");
            case wayType.WATER:
                return !hasTag("seamark:type", "ferry_route") &&
                        (tags.get("seamark:type") != null ||
                                hasTag("natural", "shoal") ||
                                hasTag("natural", "bay"));
            case wayType.AMENITIES:
                return tags.get("amenity") != null;
            case wayType.AEROWAYS:
                return tags.get("aeroway") != null;
            case wayType.MAN_MADE:
                return tags.get("man_made") != null;
            default:
                return false;
        }
    }

    //Get method for the tag value
    public String getValue() {
        if(isType(wayType.NATURAL)) return tags.get("natural");
        else if(isType(wayType.ROAD)) {
            if(!isType(wayType.ROAD)) return null;
            return tags.get("highway");
        }
        else if(isType(wayType.LANDUSE)) return tags.get("landuse");
        else if(isType(wayType.SEAROUTE)) {
            if(tags.get("seamark:type") != null) return tags.get("seamark:type");
            else if (tags.get("route") != null) return tags.get("seamark:type");
        }
        else if(isType(wayType.BUILDING)) return tags.get("building");
        else if(isType(wayType.AMENITIES)) return tags.get("amenity");
        else if(isType(wayType.AEROWAYS)) return tags.get("aeroway");
        else if(isType(wayType.MAN_MADE)) return tags.get("man_made");
        return null;
    }


}
