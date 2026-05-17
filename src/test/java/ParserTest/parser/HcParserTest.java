package ParserTest.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import models.HeightCurve;
import models.HeightCurveData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import parser.HcParser;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HcParserTest {

    //String AI degenerated
    private static final String MINIMAL_HC =
            "<heightcurves>\n" +
                    "  <hc id=\"-1\" height=\"0.0\">\n" +
                    "    <coords lat=\"-90.0\" lon=\"-180.0\"/>\n" +
                    "    <coords lat=\"-90.0\" lon=\"180.0\"/>\n" +
                    "    <coords lat=\"90.0\" lon=\"180.0\"/>\n" +
                    "    <coords lat=\"-90.0\" lon=\"-180.0\"/>\n" +
                    "  </hc>\n" +
                    "  <hc id=\"1\" height=\"2.5\">\n" +
                    "    <coords lat=\"55.880\" lon=\"10.630\"/>\n" +
                    "    <coords lat=\"55.900\" lon=\"10.650\"/>\n" +
                    "    <coords lat=\"55.880\" lon=\"10.670\"/>\n" +
                    "    <coords lat=\"55.880\" lon=\"10.630\"/>\n" +
                    "  </hc>\n" +
                    "  <hc id=\"2\" height=\"5.0\">\n" +
                    "    <coords lat=\"55.885\" lon=\"10.638\"/>\n" +
                    "    <coords lat=\"55.892\" lon=\"10.645\"/>\n" +
                    "    <coords lat=\"55.885\" lon=\"10.652\"/>\n" +
                    "    <coords lat=\"55.885\" lon=\"10.638\"/>\n" +
                    "  </hc>\n" +
                    "</heightcurves>";

    //Helper Methods:
    private InputStream toStream(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }

    private HeightCurveData parseHc() {
        return new HcParser(toStream(MINIMAL_HC)).parse();
    }

    @Test
    void correctCurveCount() {
        assertEquals(3, parseHc().curves.size());
    }

    @Test
    void seaRootHasOneDirectChild() {
        assertEquals(1, parseHc().sea.getChildren().size());
    }

    @Test
    void innerCurveNestedInsideOuter() {
        HeightCurveData data = parseHc();
        HeightCurve outer = data.sea.getChildren().get(0);
        assertEquals(2.5f, outer.getHeight(), 0.001f);
        assertEquals(1, outer.getChildren().size());
        assertEquals(5.0f, outer.getChildren().get(0).getHeight(), 001f);
    }

    @Test
    void curveHasCorrectCordCount() {
        HeightCurve outer = parseHc().sea.getChildren().get(0);
        assertEquals(4, outer.getCoords().size());
    }

    @Test
    void boundsAreNonZero() {
        HeightCurveData data = parseHc();
        assertNotEquals(0.0f, data.minLat);
        assertNotEquals(0.0f, data.maxLon);
    }

    @Test
    void minusOneIdFiltered() {
        HeightCurveData data = parseHc();
        for (HeightCurve curve : data.curves) {
            if (curve == data.sea) continue;
            assertNotEquals(-1L, curve.getId());
        }
    }
}

