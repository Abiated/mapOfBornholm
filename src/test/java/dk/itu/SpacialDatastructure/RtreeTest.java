package dk.itu.SpacialDatastructure;

import java.util.List;
import java.util.stream.Stream;

import interfaces.InsertableInRtree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import spatialDatastructures.Rtree;
import spatialDatastructures.RtreeNode;

import static org.junit.jupiter.api.Assertions.*;

public class RtreeTest {



    @BeforeEach
    public void Setup() {
    }

    private static float[] expectedBBox(List<InsertableInRtree> items) {
        float minLon = Float.MAX_VALUE;
        float maxLon = -Float.MAX_VALUE;
        float minLat = Float.MAX_VALUE;
        float maxLat = -Float.MAX_VALUE;

        for (InsertableInRtree item : items) {
            float[] bb = item.getBoundingBox();
            minLon = Math.min(minLon, bb[0]);
            maxLon = Math.max(maxLon, bb[1]);
            minLat = Math.min(minLat, bb[2]);
            maxLat = Math.max(maxLat, bb[3]);
        }

        return new float[]{minLon, maxLon, minLat, maxLat};
    }

    static Stream<List<InsertableInRtree>> provider() {
        Util.resetIds();

        return Stream.of(
                Util.nodes(new float[][] {
                        {0f, 0f}
                }),
                Util.nodes(new float[][] {
                        {0f, 0f}, {1f, 1f}, {2f, 2f}
                }),
                Util.grid(2, 2, 1f),
                Util.line(10, 0f, 0f, 1f, 0f)
        );
    }

    @ParameterizedTest
    @MethodSource("provider")
    @Order(1)
    void CreateRtree(List<InsertableInRtree> items) {
        try {
            int M = 4; // amount

            Rtree tree = new Rtree(items, M);
            RtreeNode root = tree.returnRoot();
            assertNotNull(tree);
            assertNotNull(root);
            assertNotNull(root.returnbbox());

            assertTrue(root.returnChildren().size() > 0);

        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION] : %s", e.getMessage()));
        }
    }

    @ParameterizedTest
    @MethodSource("provider")
    @Order(2)
    void Test2(List<InsertableInRtree> items) {
        try {
            int M = 4; // amount

            Rtree tree = new Rtree(items, M);
            RtreeNode root = tree.returnRoot();
            float[] bbox = root.returnbbox();
            float[] expected = expectedBBox(items);

            assertArrayEquals(expected, bbox, 0.0001f);

        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION] : %s", e.getMessage()));
        }
    }

    @ParameterizedTest
    @MethodSource("provider")
    @Order(3)
    void Test3(List<InsertableInRtree> items) {
        try {
            int M = 4; // amount

            Rtree tree = new Rtree(items, M);
            RtreeNode root = tree.returnRoot();
            float[] bbox = root.returnbbox();
            float[] expected = expectedBBox(items);

            assertArrayEquals(expected, bbox, 0.0001f);

        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION] : %s", e.getMessage()));
        }
    }

}


