package dk.itu.pathfinder;

import PathFinder.EdgeWeightedDigraph;
import PathFinder.SubmergedVertexFinder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Coordinate;
import models.HeightCurve;
import models.HeightCurveData;
import models.OsmNode;
import models.OsmWay;
import org.junit.jupiter.api.Test;
import parser.Parser;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SubmergedVertexFinderTest {

    //TODO: Many more comments to easily understand what is going on for exam
    // TODO: is is too much with the tree tests & aroundMiddelNode?

    @Test
    public void findsOnlyGraphVerticesInsideSubmergedHeightCurve() throws Exception {
        Parser parser = createParserWithOneRoadThroughThreeNodes();
        JsonNode config = createConfig();
        EdgeWeightedDigraph graph = EdgeWeightedDigraph.constructGraph(parser, config);
        HeightCurveData heightCurveData = createHeightCurveDataAroundMiddleNode();

        Set<Integer> submergedVertices = SubmergedVertexFinder.findSubmergedVertices(
                graph, parser, heightCurveData, 1.0F);

        int leftVertex = graph.indexOf(1L);
        int middleVertex = graph.indexOf(2L);
        int rightVertex = graph.indexOf(3L);

        assertEquals(1, submergedVertices.size());
        assertTrue(submergedVertices.contains(middleVertex));
        assertFalse(submergedVertices.contains(leftVertex));
        assertFalse(submergedVertices.contains(rightVertex));
    }

    @Test
    public void oldZeroBoundingBoxIsRecalculatedBeforeRtreeSearch() throws Exception {
        Parser parser = createParserWithOneRoadThroughThreeNodes();
        JsonNode config = createConfig();
        EdgeWeightedDigraph graph = EdgeWeightedDigraph.constructGraph(parser, config);
        HeightCurveData heightCurveData = createHeightCurveDataAroundMiddleNode();

        HeightCurve curve = heightCurveData.getCurves().get(1);
        Field boundingBox = HeightCurve.class.getDeclaredField("boundingBox");
        boundingBox.setAccessible(true);
        boundingBox.set(curve, new float[]{0.0F, 0.0F, 0.0F, 0.0F});

        Set<Integer> submergedVertices = SubmergedVertexFinder.findSubmergedVertices(
                graph, parser, heightCurveData, 1.0F);

        int middleVertex = graph.indexOf(2L);

        assertEquals(1, submergedVertices.size());
        assertTrue(submergedVertices.contains(middleVertex));
    }

    @Test
    public void noVerticesAreSubmergedWhenSeaLevelIsBelowHeightCurve() throws Exception {
        Parser parser = createParserWithOneRoadThroughThreeNodes();
        JsonNode config = createConfig();
        EdgeWeightedDigraph graph = EdgeWeightedDigraph.constructGraph(parser, config);
        HeightCurveData heightCurveData = createHeightCurveDataAroundMiddleNode();

        Set<Integer> submergedVertices = SubmergedVertexFinder.findSubmergedVertices(
                graph, parser, heightCurveData, -1.0F);

        assertEquals(0, submergedVertices.size());
    }

    private Parser createParserWithOneRoadThroughThreeNodes() {
        Parser parser = new Parser(new ByteArrayInputStream(new byte[0]));

        OsmNode left = new OsmNode(1L, 0.5F, 0.5F);
        OsmNode middle = new OsmNode(2L, 1.5F, 1.5F);
        OsmNode right = new OsmNode(3L, 2.5F, 2.5F);

        parser.getOsmNodeMap().put(1L, left);
        parser.getOsmNodeMap().put(2L, middle);
        parser.getOsmNodeMap().put(3L, right);

        List<OsmNode> nodes = new ArrayList<OsmNode>();
        nodes.add(left);
        nodes.add(middle);
        nodes.add(right);

        HashMap<String, String> tags = new HashMap<String, String>();
        tags.put("highway", "residential");

        OsmWay way = new OsmWay(10L, nodes, tags);
        parser.getOsmWayMap().put(10L, way);

        return parser;
    }

    private HeightCurveData createHeightCurveDataAroundMiddleNode() {
        List<Coordinate> coords = new ArrayList<Coordinate>();
        coords.add(new Coordinate(1.0F, 1.0F));
        coords.add(new Coordinate(1.0F, 2.0F));
        addRepeatedCoordinate(coords, new Coordinate(1.0F, 2.0F), 19);
        coords.add(new Coordinate(2.0F, 2.0F));
        addRepeatedCoordinate(coords, new Coordinate(2.0F, 2.0F), 19);
        coords.add(new Coordinate(2.0F, 1.0F));
        addRepeatedCoordinate(coords, new Coordinate(2.0F, 1.0F), 19);
        coords.add(new Coordinate(1.0F, 1.0F));

        HeightCurve sea = new HeightCurve(0L, -1.0F, new ArrayList<Coordinate>(), new ArrayList<HeightCurve>());
        HeightCurve lowArea = new HeightCurve(1L, 0.0F, coords, new ArrayList<HeightCurve>());
        sea.getChildren().add(lowArea);

        List<HeightCurve> curves = new ArrayList<HeightCurve>();
        curves.add(sea);
        curves.add(lowArea);

        return new HeightCurveData(0.0F, 0.0F, 3.0F, 3.0F, sea, curves);
    }

    //helper for controlled testing
    private void addRepeatedCoordinate(List<Coordinate> coords, Coordinate coordinate, int numberOfTimes) {
        int i;
        for (i = 0; i < numberOfTimes; i++) {
            coords.add(coordinate);
        }
    }

    //helper for controlled testing
    private JsonNode createConfig() throws Exception {
        String json = "{ \"highway\": { \"residential\": { \"speed\": 50, " +
                "\"driveable\": true, \"cyclable\": true, \"walkable\": true } } }";
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(json.getBytes(StandardCharsets.UTF_8));
    }
}
