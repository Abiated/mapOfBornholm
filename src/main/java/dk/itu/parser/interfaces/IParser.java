package dk.itu.parser.interfaces;

import java.util.HashMap;
import java.util.List;

import dk.itu.renderer.osm.OsmNode;
import dk.itu.renderer.osm.OsmRelation;
import dk.itu.renderer.osm.OsmWay;

public interface IParser {

    void parse();

    HashMap<Long, OsmNode> getOsmNodeMap();

    HashMap<Long, OsmWay> getOsmWayMap();

    HashMap<Long, OsmRelation> getOsmRelationMap();

    List<Float> getBoundingBox();
}
