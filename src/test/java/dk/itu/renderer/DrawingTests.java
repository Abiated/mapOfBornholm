package dk.itu.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dk.itu.parser.Parser;
import dk.itu.renderer.osm.OsmElement;
import dk.itu.renderer.osm.OsmNode;
import dk.itu.renderer.osm.OsmWay;
import dk.itu.renderer.utils.DependsOn;
import dk.itu.renderer.utils.DependsOnExtension;
import dk.itu.renderer.utils.FakeParser;
import dk.itu.renderer.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DependsOnExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DrawingTests {

    private static final Color BG = Color.decode("#aad3e0");
    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;

    private BufferedImage bufferedImage;
    private Graphics2D gc;

    @BeforeEach
    public void setup() {
        this.bufferedImage = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .createCompatibleImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB_PRE);

        this.gc = this.bufferedImage.createGraphics();

        gc.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF
        );
        gc.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE
        );
        gc.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );
    }

    private Renderer makeRenderer() {
        Parser parser = new FakeParser(List.of(0f, 0f, 800f, 800f));
        return new Renderer(gc);
    }

    private void fillBackground() {
        gc.setTransform(new AffineTransform());
        gc.setColor(BG);
        gc.fillRect(0, 0, WIDTH, HEIGHT);
    }

    @Test
    @Order(101)
    public void test_zero_nodes_are_not_drawn() {
        OsmWay wayUnderTest = new OsmWay(0, List.of(), Map.of());

        fillBackground();

        Renderer renderer = makeRenderer();
        gc.setColor(Color.BLACK);
        gc.draw(renderer.getWayPath(wayUnderTest, 2));

        Utils.savePixelsAsImageAndTest("task-2a_zero_nodes_are_not_drawn", bufferedImage, true);
    }

    @Test
    @Order(102)
    public void test_one_node_is_not_drawn() {
        OsmWay wayUnderTest = new OsmWay(
                1,
                List.of(new OsmNode(0L, 400f, 400f)),
                Map.of()
        );

        fillBackground();

        Renderer renderer = makeRenderer();
        gc.setColor(Color.BLACK);
        renderer.drawWay(wayUnderTest, 2);

        Utils.savePixelsAsImageAndTest("task-2b_one_node_is_not_drawn", bufferedImage, true);
    }

    @Test
    @Order(103)
    public void test_symmetric_line_segment() {
        OsmWay wayUnderTest = new OsmWay(
                2,
                List.of(
                        new OsmNode(0L, 100f, 100f),
                        new OsmNode(1L, 700f, 700f)
                ),
                Map.of()
        );

        fillBackground();

        Renderer renderer = makeRenderer();
        gc.setColor(Color.BLACK);
        renderer.drawWay(wayUnderTest, 2);

        Utils.savePixelsAsImageAndTest("task-2c_symmetric_line_segment", bufferedImage, true);
    }

    @Test
    @Order(104)
    public void test_asymmetric_line_segment() {
        OsmWay wayUnderTest = new OsmWay(
                3,
                List.of(
                        new OsmNode(0L, 100f, 100f),
                        new OsmNode(1L, 300f, 700f)
                ),
                Map.of()
        );

        fillBackground();

        Renderer renderer = makeRenderer();
        gc.setColor(Color.BLACK);
        renderer.drawWay(wayUnderTest, 2);

        Utils.savePixelsAsImageAndTest("task-2d_asymmetric_line_segment", bufferedImage, true);
    }

    @Test
    @Order(105)
    @DependsOn("test_asymmetric_line_segment")
    public void test_lines_with_different_widths() {
        OsmWay sideRoad = new OsmWay(
                4,
                List.of(
                        new OsmNode(0L, 400f, 400f),
                        new OsmNode(1L, 700f, 100f)
                ),
                Map.of()
        );

        OsmWay boulevard = new OsmWay(
                5,
                List.of(
                        new OsmNode(2L, 100f, 100f),
                        new OsmNode(3L, 700f, 700f)
                ),
                Map.of()
        );

        fillBackground();

        Renderer renderer = makeRenderer();

        gc.setColor(Color.ORANGE);
        renderer.drawWay(sideRoad, 3);

        gc.setColor(Color.RED);
        renderer.drawWay(boulevard, 10);

        Utils.savePixelsAsImageAndTest("task-2e_lines_with_different_widths", bufferedImage, true);
    }

    @Test
    @Order(201)
    @DependsOn("test_lines_with_different_widths")
    public void test_polygonal_chain() {
        OsmWay wayUnderTest = new OsmWay(
                6,
                List.of(
                        new OsmNode(0L, 100f, 100f),
                        new OsmNode(1L, 100f, 700f),
                        new OsmNode(2L, 700f, 700f),
                        new OsmNode(3L, 400f, 400f)
                ),
                Map.of()
        );

        fillBackground();

        Renderer renderer = makeRenderer();
        gc.setColor(Color.BLACK);
        renderer.drawWay(wayUnderTest, 2);

        Utils.savePixelsAsImageAndTest("task-3a_polygonal_chain", bufferedImage, true);
    }

    @Test
    @Order(301)
    @DependsOn("test_polygonal_chain")
    public void test_draw_polygon_forest() {
        OsmNode startEndNode = new OsmNode(0L, 100f, 100f);

        OsmWay polygon = new OsmWay(
                7,
                List.of(
                        startEndNode,
                        new OsmNode(1L, 100f, 700f),
                        new OsmNode(2L, 400f, 400f),
                        new OsmNode(3L, 700f, 700f),
                        new OsmNode(4L, 700f, 100f),
                        new OsmNode(5L, 600f, 400f),
                        new OsmNode(6L, 400f, 100f),
                        startEndNode
                ),
                Map.of("natural", "forest")
        );

        fillBackground();

        Renderer renderer = makeRenderer();
        renderer.drawWay(polygon, null);

        Utils.savePixelsAsImageAndTest("task-4a_polygon", bufferedImage, true);
    }

    @Test
    @Order(302)
    @DependsOn("test_draw_polygon_forest")
    public void test_drawing_way_polygon_doesnt_need_stroke_width() {
        OsmNode startEndNode = new OsmNode(0L, 100f, 100f);

        OsmWay polygon = new OsmWay(
                8,
                List.of(
                        startEndNode,
                        new OsmNode(1L, 100f, 700f),
                        new OsmNode(2L, 400f, 400f),
                        new OsmNode(3L, 700f, 700f),
                        new OsmNode(4L, 700f, 100f),
                        new OsmNode(5L, 600f, 400f),
                        new OsmNode(6L, 400f, 100f),
                        startEndNode
                ),
                Map.of("place", "city")
        );

        fillBackground();

        Renderer renderer = makeRenderer();
        renderer.drawWay(polygon, null);

        // Passes if no exception is thrown
    }

    @Test
    @Order(401)
    @DependsOn("test_drawing_way_polygon_doesnt_need_stroke_width")
    public void test_overlapping_polygons() {
        OsmWay island = new OsmWay(
                9,
                List.of(
                        new OsmNode(0L, 100f, 100f),
                        new OsmNode(1L, 100f, 700f),
                        new OsmNode(2L, 700f, 700f),
                        new OsmNode(3L, 700f, 100f),
                        new OsmNode(0L, 100f, 100f)
                ),
                Map.of("landuse", "residential")
        );

        OsmWay forest = new OsmWay(
                10,
                List.of(
                        new OsmNode(10L, 200f, 200f),
                        new OsmNode(11L, 250f, 600f),
                        new OsmNode(12L, 550f, 550f),
                        new OsmNode(13L, 600f, 250f),
                        new OsmNode(10L, 200f, 200f)
                ),
                Map.of("natural", "forest")
        );

        OsmWay lake = new OsmWay(
                11,
                List.of(
                        new OsmNode(20L, 350f, 350f),
                        new OsmNode(21L, 350f, 450f),
                        new OsmNode(22L, 450f, 450f),
                        new OsmNode(23L, 450f, 350f),
                        new OsmNode(20L, 350f, 350f)
                ),
                Map.of("natural", "river")
        );

        fillBackground();

        Renderer renderer = makeRenderer();
        renderer.drawWay(island, null);
        renderer.drawWay(forest, null);
        renderer.drawWay(lake, null);

        Utils.savePixelsAsImageAndTest("task-4b_overlapping_polygons", bufferedImage, true);
    }

    @Test
    @Order(501)
    @DependsOn("test_overlapping_polygons")
    public void test_bondeholm_first_attempt() {
        List<OsmElement> ways = Utils.getBondeholm();

        fillBackground();

        Renderer renderer = makeRenderer();
        for (OsmElement element : ways) {
            if (element instanceof OsmWay way) {
                if (way.getNodes().size() == 2) {
                    gc.setColor(Color.BLACK);
                    renderer.drawWay(way, 2);
                } else {
                    renderer.drawWay(way, null);
                }
            }
        }

        Utils.savePixelsAsImageAndTest("task-5a_bondeholm_first_attempt", bufferedImage, true);
    }

    @Test
    @Order(502)
    @DependsOn("test_overlapping_polygons")
    public void test_can_order_elements() {
        List<OsmElement> ways = Utils.getBondeholm();

        DrawingUtils.sortOsmElementsByArea(ways);

        assertEquals(1L, ways.get(0).getId());
        assertEquals(2L, ways.get(1).getId());
    }

    @Test
    @Order(503)
    @DependsOn("test_can_order_elements")
    public void test_bondeholm_sorted() {
        List<OsmElement> ways = Utils.getBondeholm();

        DrawingUtils.sortOsmElementsByArea(ways);

        fillBackground();

        Renderer renderer = makeRenderer();
        for (OsmElement element : ways) {
            if (element instanceof OsmWay way) {
                if (way.getNodes().size() == 2) {
                    gc.setColor(Color.BLACK);
                    renderer.drawWay(way, 2);
                } else {
                    renderer.drawWay(way, null);
                }
            }
        }

        Utils.savePixelsAsImageAndTest("task-5b_bondeholm", bufferedImage, true);
    }

    @Test
    @Order(603)
    @DependsOn("test_bondeholm_sorted")
    public void test_bonus_map() {
        int width = 1000;
        int height = 800;

        BufferedImage localImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D localGc = localImage.createGraphics();

        localGc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        localGc.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        localGc.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        localGc.setColor(BG);
        localGc.fillRect(0, 0, width, height);

        Parser parser = new FakeParser(List.of(0f, 0f, 800f, 800f));
        Renderer renderer = new Renderer(localGc);

        List<OsmElement> bonus = Utils.getBondeholm();
        DrawingUtils.sortOsmElementsByArea(bonus);

        for (OsmElement element : bonus) {
            if (element instanceof OsmWay way) {
                if (way.getNodes().size() == 2) {
                    localGc.setColor(paletteColor(way.getId()));
                    renderer.drawWay(way, 2);
                } else {
                    renderer.drawWay(way, null);
                }
            }
        }

        Utils.savePixelsAsImageAndTest("task-5c_bonus_map", localImage, true);
        localGc.dispose();
    }

    private static Color paletteColor(long id) {
        int idx = Math.floorMod(Long.hashCode(id), FRIENDLY_PALETTE.length);
        return FRIENDLY_PALETTE[idx];
    }

    private static final Color[] FRIENDLY_PALETTE = {
            new Color(0x4E79A7),
            new Color(0x59A5D8),
            new Color(0x76B7B2),
            new Color(0x59A14F),
            new Color(0x8CD17D),
            new Color(0x86BC86),
            new Color(0xEDC948),
            new Color(0xF28E2B),
            new Color(0xFFBE7D),
            new Color(0xE15759),
            new Color(0xFF9DA7),
            new Color(0xD37295),
            new Color(0xB07AA1),
            new Color(0x9C755F),
            new Color(0xBAB0AC)
    };
}