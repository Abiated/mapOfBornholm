package models;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OsmRelation extends OsmElement implements /*Renderable*/ Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<Member> members;
    //private HashMap<String, String> tags;

    public OsmRelation(long id, List<Member> members, HashMap<String, String> tags) {
        super(id, tags);
        this.members = members;
        //this.tags = tags;
    }

    //Exist for deserializable as it needs a no argument constructor, value does not matter
    private OsmRelation() {
        super(0, new HashMap<>());
        this.members = new ArrayList<>();
        //this.tags = new HashMap<>();
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    private float[] computeBoundingBox() {
        float minLon = Float.MAX_VALUE, minLat = Float.MAX_VALUE;
        float maxLon = Float.MIN_VALUE, maxLat = Float.MIN_VALUE;

        for(Member member : members) {
            if(!(member.getElement() instanceof OsmWay)) continue;
            OsmWay way = (OsmWay) member.getElement();
            if(way == null) continue;
            float[] bb = way.getBoundingBox();
            if(bb == null) continue;
            minLon = Math.min(minLon, bb[0]);
            maxLon = Math.max(maxLon, bb[1]);
            minLat = Math.min(minLat, bb[2]);
            maxLat = Math.max(maxLat, bb[3]);
        }

        if(minLon == Float.MAX_VALUE) return null;
        this.setArea((maxLon - minLon) * (maxLat - minLat));
        return new float[]{minLon, maxLon, minLat, maxLat};
    }

    @Override
    public float[] getBoundingBox() {return computeBoundingBox();}

}
