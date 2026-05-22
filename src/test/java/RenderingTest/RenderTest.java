package RenderingTest;

import models.Member;
import models.OsmNode;
import models.OsmRelation;
import models.OsmWay;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RenderTest {

    private OsmWay makeWay(long id, String tagKey, String tagVal, OsmNode... nodes) {
        HashMap<String, String> tags = new HashMap<>();
        if(tagKey != null) tags.put(tagKey, tagVal);
        return new OsmWay(id, List.of(nodes), tags);
    }

    private Graphics2D makeGc() {
        return new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB)
                .createGraphics();
    }

    private rendering.Renderer makeRenderer() {
        return new  rendering.Renderer(makeGc(), null);
    }

    //Way
    @Test
    @Order(1)
    void getWayPath_twoNodes() {
        OsmNode n1 = new OsmNode(1, 55.89f, 10.45f);
        OsmNode n2 = new OsmNode(2, 55.90f, 10.46f);
        OsmWay way = makeWay(1, "highway", "residential", n1, n2);

        Path2D path = makeRenderer().getWayPath(way);

        assertNotNull(path);

        Rectangle2D bounds = path.getBounds2D();
        assertTrue(bounds.getWidth() > 0 || bounds.getHeight() > 0);
    }

    @Test
    @Order(2)
    void getWayPath_closedWay() {
        OsmNode n1 = new OsmNode(1, 55.89f, 10.45f);
        OsmNode n2 = new OsmNode(2, 55.90f, 10.46f);
        OsmNode n3 = new OsmNode(3, 55.91f, 10.47f);
        OsmWay way = makeWay(1, "natural", "wood", n1, n2, n3, n1);

        Path2D path = makeRenderer().getWayPath(way);

        assertNotNull(path);
        assertFalse(path.getBounds2D().isEmpty(),
                "Closed polygon path should have non-zero bounding area");
    }

    @Test
    @Order(3)
    void getWayPath_emptyWay() {
        OsmWay way = makeWay(9, null, null);

        Path2D path = makeRenderer().getWayPath(way);

        assertNotNull(path, "An empty path should not be null");
        assertTrue(path.getBounds2D().isEmpty());
    }

    @Test
    @Order(4)
    void getWayPath_singleNode() {
        OsmNode n1 = new OsmNode(1, 55.89f, 10.45f);
        OsmWay way = makeWay(1, "highway", "residential", n1);

        Path2D path = makeRenderer().getWayPath(way);

        assertNotNull(path);
        assertTrue(path.getBounds2D().isEmpty());
    }

    //MultiPolygon
    @Test
    @Order(5)
    void buildMultiPolygon_nullTags() {
        OsmRelation rel = new OsmRelation(1L, new ArrayList<>(), null);
        assertNull(makeRenderer().buildMultiPolygon(rel));
    }

    @Test
    @Order(6)
    void buildMultiPolygon_notMultipolygon() {
        HashMap<String, String> tags = new HashMap<>();
        tags.put("type", "route");
        OsmRelation rel = new OsmRelation(1L, new ArrayList<>(), tags);

        assertNull(makeRenderer().buildMultiPolygon(rel));
    }

    @Test
    @Order(7)
    void buildMultiPolygon_notOuterWays_returnsNull() {
        OsmNode n1 = new OsmNode(1, 55.88f, 10.63f);
        OsmNode n2 = new OsmNode(2, 55.90f, 10.65f);
        OsmWay inner = makeWay(10, "natural", "wood", n1, n2, n1);

        HashMap<String, String> tags = new HashMap<>();
        tags.put("type", "multipolygon");

        List<Member> members = new ArrayList<>();
        members.add(new Member(inner, "inner"));

        OsmRelation rel = new OsmRelation(10L, members, tags);

        assertNull(makeRenderer().buildMultiPolygon(rel),
                "Relation with no outer ways should return null");
    }

    @Test
    @Order(8)
    void buildMultiPolygon_withoutInnerPaths() {
        OsmNode n1 = new OsmNode(1, 55.880f, 10.63f);
        OsmNode n2 = new OsmNode(2, 55.900f, 10.65f);
        OsmNode n3 = new OsmNode(3, 55.880f, 10.67f);
        OsmWay outer = makeWay(10, "natural", "wood", n1, n2, n3, n1);

        HashMap<String, String> tags = new HashMap<>();
        tags.put("type", "multipolygon");

        List<Member> members = new ArrayList<>();
        members.add(new Member(outer, "outer"));

        OsmRelation rel = new OsmRelation(10L, members, tags);

        assertNotNull(makeRenderer().buildMultiPolygon(rel));
    }

    @Test
    @Order(9)
    void buildMultiPolygon_withInnerPaths() {
        OsmNode o1 = new OsmNode(1, 55.870f, 10.620f);
        OsmNode o2 = new OsmNode(2, 55.910f, 10.660f);
        OsmNode o3 = new OsmNode(3, 55.870f, 10.700f);
        OsmWay outer = makeWay(10, "natural", "wood", o1, o2, o3, o1);

        OsmNode i1 = new OsmNode(1, 55.882f, 10.63f);
        OsmNode i2 = new OsmNode(2, 55.90f, 10.65f);
        OsmNode i3 = new OsmNode(3, 55.92f, 10.67f);
        OsmWay inner = makeWay(10, "natural", "wood", i1, i2, i3, i1);

        HashMap<String, String> tags = new HashMap<>();
        tags.put("type", "multipolygon");

        List<Member> members = new ArrayList<>();
        members.add(new Member(outer, "outer"));
        members.add(new Member(inner, "inner"));

        OsmRelation rel = new OsmRelation(10L, members, tags);

        Path2D path = makeRenderer().buildMultiPolygon(rel);
        assertNotNull(path);

        Path2D outerOnly = makeRenderer().getWayPath(outer);
        assertTrue(
                path.getBounds2D().getWidth() >= outerOnly.getBounds2D().getWidth() - 0.001);
    }




}
