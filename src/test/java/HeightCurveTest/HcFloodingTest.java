package HeightCurveTest;

import models.Coordinate;
import models.HeightCurve;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HcFloodingTest {

    private HeightCurve makeCurve(long id, float height) {
        List<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(55.880f, 10.630f));
        coords.add(new Coordinate(55.900f, 10.650f));
        coords.add(new Coordinate(55.880f, 10.670f));
        coords.add(new Coordinate(55.880f, 10.630f));

        return new HeightCurve(id, height, coords);
    }

    @Test
    @Order(1)
    void submergeFloodParentAndChild() {
        HeightCurve parent = makeCurve(1, 5.0f);
        HeightCurve child = makeCurve(2, 3.0f);
        parent.getChildren().add(child);

        parent.submerge(6.0f);

        assertTrue(parent.isSubmerged());
        assertTrue(child.isSubmerged());
    }

    @Test
    @Order(2)
    void submergeTree() {
        HeightCurve parent = makeCurve(1, 5.0f);
        HeightCurve child = makeCurve(2, 3.0f);
        HeightCurve grand = makeCurve(2, 1.0f);
        child.getChildren().add(grand);
        parent.getChildren().add(child);

        parent.submerge(7.0f);

        assertTrue(parent.isSubmerged());
        assertTrue(child.isSubmerged());
        assertTrue(grand.isSubmerged());
    }

    @Test
    @Order(3)
    void submergeCurveAboveSeaLevelIsNotFlooded() {
        HeightCurve curve = makeCurve(1, 10.0f);
        curve.submerge(6.0f);
        assertFalse(curve.isSubmerged());
    }

    @Test
    @Order(4)
    void submergeDryParentDoNotPropagateToChild() {
        HeightCurve parent = makeCurve(1, 5.0f);
        HeightCurve child = makeCurve(2, 3.0f);
        parent.getChildren().add(child);

        float newSeaLevel = 4.0f;
        parent.submerge(newSeaLevel);

        assertTrue(child.getHeight() < newSeaLevel, "SeaLevel should be above child curves height");
        assertFalse(parent.isSubmerged());
        assertFalse(child.isSubmerged(), "Child should NOT flood when parent is dry");
    }

    @Test
    @Order(5)
    void resetSubmergedClearDescendants() {
        HeightCurve parent = makeCurve(1, 5.0f);
        HeightCurve child = makeCurve(2, 3.0f);
        HeightCurve grand = makeCurve(2, 1.0f);
        child.getChildren().add(grand);
        parent.getChildren().add(child);

        parent.submerge(10.0f);
        assertTrue(parent.isSubmerged() && child.isSubmerged()
                    && grand.isSubmerged(), "All curves should be submerge after flood");

        //ResetSubmerge
        parent.resetSubmerged();

        assertFalse(parent.isSubmerged());
        assertFalse(child.isSubmerged());
        assertFalse(grand.isSubmerged());
    }

    @Test
    @Order(6)
    void submergeOnlyStrictlyLess() {
        HeightCurve curve = makeCurve(1, 5.0f);

        //Equal height
        curve.submerge(5.0f);
        assertFalse(curve.isSubmerged());

        //Above height
        curve.submerge(5.1f);
        assertTrue(curve.isSubmerged());
    }

    @Test
    @Order(7)
    void hcBoundingBoxCoversAllCorrds() {
        HeightCurve curve = makeCurve(1, 5.0f);
        float[] bb = curve.getBoundingBox();

        assertEquals(10.630f, bb[0], 0.001f);
        assertEquals(10.670f, bb[1], 0.001f);
        assertEquals(55.880f, bb[2], 0.001f);
        assertEquals(55.900f, bb[3], 0.001f);

    }


}
