package dk.itu.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import dk.itu.renderer.osm.OsmElement;
import dk.itu.renderer.osm.OsmNode;
import dk.itu.renderer.osm.OsmWay;
import dk.itu.renderer.utils.DependsOn;
import dk.itu.renderer.utils.DependsOnExtension;
import dk.itu.renderer.utils.Utils;

@ExtendWith(DependsOnExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DrawingTests {

    private static final Color BG = Color.decode("#aad3e0");
    private static final int WIDTH = 800, HEIGHT = 800;
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

        // Make rendering more deterministic
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

    @Test
    @Order(101)
    public void test_zero_nodes_are_not_drawn() {
        // Arrange
        // Setup way to draw
        OsmWay wayUnderTest = new OsmWay(0, List.of(), Map.of());
        // Setup canvas
        gc.setColor(BG);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Act
        wayUnderTest.drawForTest(gc, Color.BLACK, 2);

        // Draw output test image
        Utils.savePixelsAsImageAndTest("task-2a_zero_nodes_are_not_drawn", bufferedImage, false);
    }

    @Test
    @Order(102)
    public void test_one_node_is_not_drawn() {
        // Arrange
        // Setup way to draw
        List<OsmNode> nodes = List.of(
                new OsmNode(0, 10, 10)
        );
        OsmWay wayUnderTest = new OsmWay(0, nodes, Map.of());
        // Setup canvas
        gc.setColor(BG);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Act
        wayUnderTest.drawForTest(gc, Color.BLACK, 2);

        // Draw output test image
        Utils.savePixelsAsImageAndTest("task-2b_one_node_is_not_drawn", bufferedImage, false);
    }

    @Test
    @Order(103)
    public void test_symmetric_line_segment() {
        // Arrange
        // Setup way to draw
        List<OsmNode> nodes = List.of(
                new OsmNode(0, 100, 100),
                new OsmNode(1, 700, 700)
        );
        OsmWay wayUnderTest = new OsmWay(0, nodes, Map.of());
        // Setup canvas
        gc.setColor(BG);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Act
        wayUnderTest.drawForTest(gc, Color.BLACK, 2);

        // Draw output test image
        Utils.savePixelsAsImageAndTest("task-2c_symmetric_line_segment", bufferedImage, false);
    }

    @Test
    @Order(104)
    public void test_asymmetric_line_segment() {
        // Arrange
        // Setup way to draw
        List<OsmNode> nodes = List.of(
                new OsmNode(0, 100, 100),
                new OsmNode(1, 300, 700)
        );
        OsmWay wayUnderTest = new OsmWay(0, nodes, Map.of());
        // Setup canvas
        gc.setColor(BG);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Act
        wayUnderTest.drawForTest(gc, Color.BLACK, 2);

        // Draw output test image
        Utils.savePixelsAsImageAndTest("task-2d_asymmetric_line_segment", bufferedImage, false);
    }

    @Test
    @Order(105)
    @DependsOn("test_asymmetric_line_segment")
    public void test_lines_with_different_colours_and_widths() {
        // Arrange
        // Setup way to draw
        List<OsmWay> ways = List.of(
                // Boulevard sideroad
                new OsmWay(
                        0,
                        List.of(
                                new OsmNode(0, 400, 400),
                                new OsmNode(1, 700, 100)
                        ),
                        Map.of()
                ),
                // Boulevard
                new OsmWay(
                        1,
                        List.of(
                                new OsmNode(2, 100, 100),
                                new OsmNode(3, 700, 700)
                        ),
                        Map.of()
                )
        );
        // Setup canvas
        gc.setColor(BG);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Act
        ways.get(0).drawForTest(gc, Color.ORANGE, 3);
        ways.get(1).drawForTest(gc, Color.RED, 10);

        // Draw output test image
        Utils.savePixelsAsImageAndTest("task-2e_lines_with_different_colours_and_widths", bufferedImage, false);
    }

    @Test
    @Order(201)
    @DependsOn("test_lines_with_different_colours_and_widths")
    public void test_polygonal_chain() {
        // Arrange
        // Setup way to draw
        List<OsmNode> nodes = List.of(
                new OsmNode(0, 100, 100),
                new OsmNode(0, 100, 700),
                new OsmNode(1, 700, 700),
                new OsmNode(1, 400, 400)
        );
        OsmWay wayUnderTest = new OsmWay(0, nodes, Map.of());
        // Setup canvas
        gc.setColor(BG);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Act
        wayUnderTest.drawForTest(gc, Color.BLACK, 2);

        // Draw output test image
        Utils.savePixelsAsImageAndTest("task-3a_polygonal_chain", bufferedImage, false);
    }

    @Test
    @Order(301)
    @DependsOn("test_polygonal_chain")
    public void test_draw_polygon() {
        // Arrange
        // Start/end node
        OsmNode startEndNode = new OsmNode(0, 100, 100);
        // Setup way to draw
        OsmWay island = new OsmWay(
                0,
                List.of(
                        startEndNode,
                        new OsmNode(1, 100, 700),
                        new OsmNode(2, 400, 400),
                        new OsmNode(3, 700, 700),
                        new OsmNode(4, 700, 100),
                        new OsmNode(5, 600, 400),
                        new OsmNode(6, 400, 100),
                        startEndNode
                ),
                Map.of()
        );
        // Setup canvas
        gc.setColor(BG);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Act
        island.drawForTest(gc, Color.decode("#f5dcba"), null);

        // Draw output test image
        Utils.savePixelsAsImageAndTest("task-4a_polygon", bufferedImage, false);
    }

    @Test
    @Order(302)
    @DependsOn("test_draw_polygon")
    public void test_drawing_way_polygon_doesnt_need_stroke_width() {
        // Arrange
        // Start/end node
        OsmNode startEndNode = new OsmNode(0, 100, 100);
        // Setup way to draw
        OsmWay island = new OsmWay(
                0,
                List.of(
                        startEndNode,
                        new OsmNode(1, 100, 700),
                        new OsmNode(2, 400, 400),
                        new OsmNode(3, 700, 700),
                        new OsmNode(4, 700, 100),
                        new OsmNode(5, 600, 400),
                        new OsmNode(6, 400, 100),
                        startEndNode
                ),
                Map.of()
        );
        // Setup canvas
        gc.setColor(BG);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Act
        island.drawForTest(gc, Color.PINK, null);

        // Should pass if no exceptions are thrown
    }

    @Test
    @Order(401)
    @DependsOn("test_drawing_way_polygon_doesnt_need_stroke_width")
    public void test_polygons_with_opacity() {
        // Arrange
        // Start/end node
        OsmNode startEndNodeIsland = new OsmNode(0, 100, 100);
        OsmNode startEndNodeFlooded = new OsmNode(7, 200, 200);
        // Setup way to draw
        List<OsmWay> ways = List.of(
                new OsmWay(
                        0,
                        List.of(
                                startEndNodeIsland,
                                new OsmNode(1, 100, 700),
                                new OsmNode(2, 400, 400),
                                new OsmNode(3, 700, 700),
                                new OsmNode(4, 700, 100),
                                new OsmNode(5, 600, 400),
                                new OsmNode(6, 400, 100),
                                startEndNodeIsland
                        ),
                        Map.of()
                ),
                new OsmWay(
                        1,
                        List.of(
                                startEndNodeFlooded,
                                new OsmNode(8, 200, 600),
                                new OsmNode(9, 300, 400),
                                startEndNodeFlooded
                        ),
                        Map.of()
                )
        );
        // Setup canvas
        gc.setColor(BG);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Act
        ways.get(0).drawForTest(gc, Color.decode("#f5dcba"), null);
        ways.get(1).drawForTest(gc, new Color(0x40739e80, true), null);

        // Draw output test image
        Utils.savePixelsAsImageAndTest("task-4b_polygons_with_opacity", bufferedImage, false);
    }

    @Test
    @Order(501)
    @DependsOn("test_polygons_with_opacity")
    public void test_bondeholm_first_attempt() {
        List<OsmElement> ways = Utils.getBondeholm();

        // Setup canvas
        gc.setColor(BG);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        double sf = HEIGHT / 0.003;

        AffineTransform transform = AffineTransform.getTranslateInstance(-0.56 * 10.581647, 54.919275);
        transform.preConcatenate(AffineTransform.getScaleInstance(sf, sf));
        gc.setTransform(transform);

        ways.get(0).drawForTest(gc, Color.decode("#cdebb0"), null);
        ways.get(1).drawForTest(gc, Color.decode("#aad3e0"), null);
        ways.get(2).drawForTest(gc, Color.decode("#f3efe9"), null);

        Utils.savePixelsAsImageAndTest("task-5a_bondeholm_first_attempt", bufferedImage, true);
    }

    @Test
    @Order(502)
    @DependsOn("test_polygons_with_opacity")
    public void test_can_order_elements() {
        List<OsmElement> ways = Utils.getBondeholmNoTransformation();

        DrawingUtils.sortOsmElementsByArea(ways);

        assertEquals(4101276, ways.get(0).getId());
        assertEquals(617915629, ways.get(1).getId());
        assertEquals(617915628, ways.get(2).getId());
    }

    @Test
    @Order(503)
    @DependsOn("test_polygons_with_opacity")
    public void test_bondeholm() {
        List<OsmElement> waysNoTransformation = Utils.getBondeholmNoTransformation();
        List<OsmElement> bondeholm = Utils.getBondeholm();

        DrawingUtils.sortOsmElementsByArea(waysNoTransformation);

        List<OsmElement> ways = waysNoTransformation
                .stream()
                .map(o -> bondeholm
                .stream()
                .filter(oo -> o.getId() == oo.getId())
                .findFirst()
                .orElseThrow(AssertionError::new)
                )
                .toList();

        // Setup canvas
        gc.setColor(BG);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        double sf = HEIGHT / 0.003;

        AffineTransform transform = AffineTransform.getTranslateInstance(-0.56 * 10.581647, 54.919275);
        transform.preConcatenate(AffineTransform.getScaleInstance(sf, sf));
        gc.setTransform(transform);

        ways.get(0).drawForTest(gc, Color.decode("#f3efe9"), null);
        ways.get(1).drawForTest(gc, Color.decode("#cdebb0"), null);
        ways.get(2).drawForTest(gc, Color.decode("#aad3e0"), null);

        Utils.savePixelsAsImageAndTest("task-5b_bondeholm", bufferedImage, true);
    }

    @Test
    @Order(603)
    @DependsOn("test_polygons_with_opacity")
    public void test_bonus_map() {
        int width = 1000;
        int height = 800;
        BufferedImage localImage
                = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D localGc = localImage.createGraphics();

        List<OsmElement> bonus = Utils.getBonusMap();
        DrawingUtils.sortOsmElementsByArea(bonus);
        localGc.setColor(BG);
        localGc.fillRect(0, 0, width, height);
        for (OsmElement x : bonus) {
            Color c = paletteColor(x.getId());
            x.drawForTest(localGc, c, null);
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
