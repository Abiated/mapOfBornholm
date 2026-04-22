package models;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OsmRelation extends OsmElement implements Serializable {
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
