package models;

import java.util.List;
import java.util.Map;

public class OsmWay extends OsmElement{

    private final List<OsmNode> nodes;
    private final Map<String, String> tags;

    public OsmWay(long id, List<OsmNode> nodes, Map<String, String> tags) {
        super(id);
        this.nodes = nodes;
        this.tags = tags;
        float xHighest = 0;
        float yHighest = 0;
        for(OsmNode Node : nodes){
            if (Node.getLat() > yHighest){
                yHighest = Node.getLat();
            }
            if (Node.getLon() > xHighest){
                xHighest = Node.getLon();
            }
        }
        this.setArea(xHighest*yHighest);

    }

    public List<OsmNode> getNodes() {
        return nodes;
    }

    public Map<String, String> getTags() {
        return tags;
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
