package dk.itu.SpacialDatastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import interfaces.InsertableInRtree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import interfaces.IParser;
import dk.itu.parser.util.models.ParserResult;
import models.Member;
import models.OsmElement;
import models.OsmNode;
import models.OsmRelation;
import models.OsmWay;
import dk.itu.renderer.utils.DependsOn;
import dk.itu.renderer.utils.DependsOnExtension;
import spatialDatastructures.Rtree;
import spatialDatastructures.RtreeNode;

import static org.junit.jupiter.api.Assertions.*;

public class RtreeTest {



    @BeforeEach
    public void Setup() {
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

            assertEquals(0f, bbox[0]);
            assertEquals(10f, bbox[1]);
            assertEquals(0f, bbox[2]);
            assertEquals(10f, bbox[3]);

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

            assertEquals(0f, bbox[0]);
            assertEquals(10f, bbox[1]);
            assertEquals(0f, bbox[2]);
            assertEquals(10f, bbox[3]);

        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION] : %s", e.getMessage()));
        }
    }


}


