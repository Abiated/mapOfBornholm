package dk.itu.renderer.osm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OsmWay extends OsmElement{

    private final List<OsmNode> nodes;
    private final HashMap<String, String> tags;

    public OsmWay(long id, List<OsmNode> nodes, Map<String, String> tags) {
        super(id);
        this.nodes = nodes;
        this.tags = new HashMap<>(tags);
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

    public HashMap<String, String> getTags() {
        return tags;
    }
}
