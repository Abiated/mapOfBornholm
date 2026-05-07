package models;

import interfaces.InsertableInRtree;
import interfaces.Renderable;
import rendering.wayType;
import spatialDatastructures.RtreeNode;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OsmWay extends OsmElement implements Serializable, InsertableInRtree, Renderable {
    private static final long serialVersionUID = 1L;

    private final List<OsmNode> nodes;
    private final Map<String, String> tags;
    private float[] boundingBox;
    private Shape cachedShape;

    //Exist for deserializable as it needs a no argument constructor, value does not matter
    private OsmWay() {
        super(0);
        this.nodes = new ArrayList<>();
        this.tags = new HashMap<>();
    }

    public OsmWay(long id, List<OsmNode> nodes, Map<String, String> tags) {
        super(id);
        this.nodes = nodes;
        this.tags = tags;
        float xHighest = 0;
        float xLowest = Float.MAX_VALUE;
        float yHighest = 0;
        float yLowest = Float.MAX_VALUE;
        for(OsmNode Node : nodes){
            if (Node.getLat() > yHighest){
                yHighest = Node.getLat();
            }
            if (Node.getLat() < yLowest){
                yLowest = Node.getLat();
            }
            if (Node.getLon() > xHighest){
                xHighest = Node.getLon();
            }
            if (Node.getLon() < xLowest){
                xLowest = Node.getLon();
            }
        }
        this.setArea(xHighest*yHighest);
        boundingBox = new float[4];
        boundingBox[0] = xLowest;
        boundingBox[1] = xHighest;
        boundingBox[2] = yLowest;
        boundingBox[3] = yHighest;
    }

    public void setCachedShape(Shape cachedShape) {
        this.cachedShape = cachedShape;
    }

    public Shape getCachedShape() {
        return this.cachedShape;
    }

    public List<OsmNode> getNodes() {
        return nodes;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public float[] getBoundingBox() { return boundingBox; }

    public boolean intersects(float[] otherBbox) {
        return !(boundingBox[1] < otherBbox[0] ||
                boundingBox[0] > otherBbox[1] ||
                boundingBox[3] < otherBbox[2] ||
                boundingBox[2] > otherBbox[3]);
    }

    //–––––––––Tag Handling–––––––––//

    //Checker if tag exist
    @Override
    public boolean hasTag(String key, String value) {
        return value.equals(tags.get(key));
    }

    @Override
    //Tag checker
    public boolean isType(wayType type) {
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
                return hasTag("natural", "coastline");
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
