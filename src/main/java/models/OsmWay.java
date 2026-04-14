package models;

import spatialDatastructures.RtreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OsmWay extends OsmElement{

    private final List<OsmNode> nodes;
    private final Map<String, String> tags;
    private float[] boundingBox;

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
    public boolean hasTag(String key, String value) {
        return value.equals(tags.get(key));
    }

    //Checker for road
    public boolean isRoad() {
        return tags.get("highway") != null;
    }
    public String getHighWayType() {
        if (!isRoad()) return null;
        return tags.get("highway");
    }

    //Checker for landuse
    public boolean isLandues() {
        return tags.get("landuse") != null;
    }
    public String getLanduseType() {
        if(!isLandues()) return null;
        return tags.get("landuse");
    }

    //checker for natural
    public boolean isNatural() {
        return tags.get("natural") != null && !hasTag("natural", "water") && !hasTag("natural", "coastline");
    }
    public String getValue() {
        if(isNatural()) return tags.get("natural");
        if(isRoad()) return tags.get("highway");
        if (isLandues()) return tags.get("landuse");
        else return null;
    }

    //Checker for water body
    public boolean isWater() {
        return hasTag("natural", "water"); //||
        //hasTag("landuse", "reservoir");
    }
    //Checker for building tag
    public boolean isBuilding() {
        return tags.get("building") != null;
    }

    public boolean isLeisure() {
        return tags.get("leisure") != null;
    }

}
