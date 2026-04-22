package Binary;

import models.HeightCurveData;
import models.OsmNode;
import models.OsmRelation;
import models.OsmWay;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class MapData implements Serializable {
    public final HeightCurveData hcData;
    public final HashMap<Long, OsmWay> wayMap;
    public final HashMap<Long, OsmNode> nodeMap;
    private final HashMap<Long, OsmRelation> osmRelationMap;
    public final List<Float> boundingBox;

    public MapData(HeightCurveData hcData, HashMap<Long, OsmWay> wayMap, HashMap<Long, OsmNode> nodeMap,
                   HashMap<Long, OsmRelation> osmRelationMap, List<Float> boundingBox) {
        this.hcData = hcData;
        this.wayMap = wayMap;
        this.nodeMap = nodeMap;
        this.osmRelationMap = osmRelationMap;
        this.boundingBox = boundingBox;
    }


}
