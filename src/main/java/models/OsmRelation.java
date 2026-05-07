package models;

import interfaces.Renderable;
import rendering.wayType;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OsmRelation extends OsmElement implements Renderable, Serializable {
    private static final long serialVersionUID = 1L;

    private List<Member> members;
    private HashMap<String, String> tags;

    public OsmRelation(long id, List<Member> members, HashMap<String, String> tags) {
        super(id);
        this.members = members;
        this.tags = tags;
    }

    //Exist for deserializable as it needs a no argument constructor, value does not matter
    private OsmRelation() {
        super(0);
        this.members = new ArrayList<>();
        this.tags = new HashMap<>();
    }

    @Override
    public boolean isType(wayType type) {
        return false;
    }
    @Override
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
    @Override
    public boolean hasTag(String key, String value) {
        return value.equals(tags.get(key));
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public HashMap<String, String> getTags() {
        return tags;
    }

    public void setTags(HashMap<String, String> tags) {
        this.tags = tags;
    }

}
