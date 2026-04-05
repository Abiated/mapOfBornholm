package dk.itu.renderer.osm;

import java.awt.Color;
import java.awt.Graphics2D;
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
    }
