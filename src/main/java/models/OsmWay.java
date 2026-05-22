package models;

import interfaces.InsertableInRtree;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OsmWay extends OsmElement implements Serializable, InsertableInRtree {
    @Serial
    private static final long serialVersionUID = 1L;

    private final List<OsmNode> nodes;
    private float[] boundingBox;
    private Shape cachedShape;

    //Exist for deserializable as it needs a no argument constructor, value does not matter
    private OsmWay() {
        super(0, new HashMap<>());
        this.nodes = new ArrayList<>();
    }

    public OsmWay(long id, List<OsmNode> nodes, HashMap<String, String> tags) {
        super(id, tags);
        this.nodes = nodes;
        float xHighest = Float.MIN_VALUE, xLowest = Float.MAX_VALUE;
        float yHighest = Float.MIN_VALUE, yLowest = Float.MAX_VALUE;
        for(OsmNode Node : nodes){
            if (Node.getLat() > yHighest)   yHighest = Node.getLat();
            if (Node.getLat() < yLowest)    yLowest = Node.getLat();
            if (Node.getLon() > xHighest)   xHighest = Node.getLon();
            if (Node.getLon() < xLowest)    xLowest = Node.getLon();
        }
        boundingBox = new float[4];
        boundingBox[0] = xLowest; boundingBox[1] = xHighest;
        boundingBox[2] = yLowest; boundingBox[3] = yHighest;
        this.setArea((boundingBox[1] - boundingBox[0]) * (boundingBox[3] - boundingBox[2]));
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

    @Override
    public float[] getBoundingBox() { return boundingBox; }

    public boolean isClosed() {
        return nodes.getFirst().getId() == nodes.getLast().getId();
    }

}
