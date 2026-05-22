package spatialDatastructures;
import interfaces.InsertableInRtree;
import models.OsmWay;

import java.util.List;

public class RtreeNode {
    List<?> children; // List<Node> or List<OsmWay>
    boolean leaf;
    float[] bbox;

    RtreeNode(List<?> children, boolean leaf) {
        this.children = children;
        this.leaf = leaf;
        this.bbox = computeBBox();
    }

    private float[] computeBBox() {
        if(children.isEmpty()) return new float[] {0, 0, 0, 0};

        float[] first = getBBox(children.get(0));

        float minX = first[0];
        float maxX = first[1];
        float minY = first[2];
        float maxY = first[3];

        for (Object obj : children) {
            float[] bb = getBBox(obj);
            minX = Math.min(minX, bb[0]);
            maxX = Math.max(maxX, bb[1]);
            minY = Math.min(minY, bb[2]);
            maxY = Math.max(maxY, bb[3]);
        }

        return new float[] {minX,maxX,minY,maxY};
    }

    private float[] getBBox(Object obj) {
        if (obj instanceof RtreeNode) return ((RtreeNode) obj).bbox;
        return ((InsertableInRtree) obj).getBoundingBox();
    }

    boolean intersects(float[] otherBbox) {
        return !(this.bbox[1] < otherBbox[0] ||
                this.bbox[0] > otherBbox[1] ||
                this.bbox[3] < otherBbox[2] ||
                this.bbox[2] > otherBbox[3]);
    }

    public float[] returnbbox(){ return this.bbox; }

    public List<?> returnChildren(){return this.children;}
}
