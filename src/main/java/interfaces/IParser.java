package interfaces;

import java.util.HashMap;
import java.util.List;

import models.OsmNode;
import models.OsmRelation;
import models.OsmWay;

public interface IParser {

    void parse();

    HashMap<Long, OsmNode> getOsmNodeMap();

    HashMap<Long, OsmWay> getOsmWayMap();

    HashMap<Long, OsmRelation> getOsmRelationMap();

    List<Float> getBoundingBox();
}
