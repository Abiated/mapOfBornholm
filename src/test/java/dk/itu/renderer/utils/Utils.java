package dk.itu.renderer.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dk.itu.renderer.osm.OsmElement;
import dk.itu.renderer.osm.OsmNode;
import dk.itu.renderer.osm.OsmWay;

public final class Utils {

    public static void savePixelsAsImage(String testName, BufferedImage image) {
        try {
            File file = new File("./testResults/" + testName + ".png");
            file.delete();
            ImageIO.write(image, "PNG", file);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static BufferedImage loadImage(String testName) {
        try {
            File file = new File("./src/test/resources/correctResults/" + testName + ".png");
            if (file.exists()) {
                BufferedImage originalImage = ImageIO.read(file);
                BufferedImage convertedImage = new BufferedImage(
                        originalImage.getWidth(),
                        originalImage.getHeight(),
                        BufferedImage.TYPE_INT_ARGB_PRE
                );
                convertedImage.getGraphics().drawImage(originalImage, 0, 0, null);
                return convertedImage;
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void savePixelsAsImageAndTest(
            String testName,
            BufferedImage image,
            boolean softFail
    ) {
        Utils.savePixelsAsImage(testName, image);
        BufferedImage correctImage;
        try {
            correctImage = Utils.loadImage(testName);
        } catch (Exception e) {
            if (softFail) {
                System.err.println(
                        "Info: [" + testName + ".png]: "
                        + "No reference image found, skipping comparison"
                );
                return;
            } else {
                throw e;
            }
        }

        if (correctImage == null) {
            if (softFail) {
                System.err.println(
                        "Info: [" + testName + ".png]: "
                        + "No reference image found, skipping comparison"
                );
                return;
            } else {
                throw new IllegalStateException(
                        "Reference image missing: " + testName + ".png"
                );
            }
        }

        int w = image.getWidth();
        int h = image.getHeight();

        //
        // Since rendering is not pixel-perfect, we compute the L_2-distance between the two images below
        //
        long count = (long) w * h * 4;
        double sumSq = 0.0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int a = image.getRGB(x, y);
                int b = correctImage.getRGB(x, y);

                int da = ((a >> 24) & 0xff) - ((b >> 24) & 0xff);
                int dr = ((a >> 16) & 0xff) - ((b >> 16) & 0xff);
                int dg = ((a >> 8) & 0xff) - ((b >> 8) & 0xff);
                int db = (a & 0xff) - (b & 0xff);

                sumSq += da * da;
                sumSq += dr * dr;
                sumSq += dg * dg;
                sumSq += db * db;
            }
        }

        double rmse = Math.sqrt(sumSq / count);

        // Tunable threshold
        double threshold = 2.0;

        String filename = testName + ".png";

        if (softFail) {
            if (rmse > threshold) {
                System.err.println(
                        "WARNING [" + filename + "]: Rendering differs slightly (RMSE = " + rmse + ")"
                );
            }
        } else {
            assertTrue(
                    rmse <= threshold,
                    "Images are too different " + filename
                    + " (RMSE = " + rmse
                    + ", threshold = " + threshold + ")"
            );
        }
    }

    public static List<OsmElement> getBondeholm() {
        List<OsmElement> elements = new ArrayList<>();

        // Water (largest polygon - should be drawn first)
        elements.add(
                new OsmWay(
                        1,
                        List.of(
                                new OsmNode(0, 50f, 50f),
                                new OsmNode(1, 50f, 750f),
                                new OsmNode(2, 750f, 750f),
                                new OsmNode(3, 750f, 50f),
                                new OsmNode(0, 50f, 50f)
                        ),
                        Map.of("natural", "water")
                )
        );

        //  Land mass (island)
        elements.add(
                new OsmWay(
                        2,
                        List.of(
                                new OsmNode(10, 150f, 150f),
                                new OsmNode(11, 150f, 650f),
                                new OsmNode(12, 650f, 650f),
                                new OsmNode(13, 650f, 150f),
                                new OsmNode(10, 150f, 150f)
                        ),
                        Map.of("landuse", "grass")
                )
        );

        // Forest area (inside island)
        elements.add(
                new OsmWay(
                        3,
                        List.of(
                                new OsmNode(20, 200f, 200f),
                                new OsmNode(21, 300f, 600f),
                                new OsmNode(22, 500f, 550f),
                                new OsmNode(23, 600f, 300f),
                                new OsmNode(24, 400f, 200f),
                                new OsmNode(20, 200f, 200f)
                        ),
                        Map.of("natural", "forest")
                )
        );

        //City area
        elements.add(
                new OsmWay(
                        4,
                        List.of(
                                new OsmNode(30, 300f, 300f),
                                new OsmNode(31, 300f, 500f),
                                new OsmNode(32, 500f, 500f),
                                new OsmNode(33, 500f, 300f),
                                new OsmNode(30, 300f, 300f)
                        ),
                        Map.of("place", "city")
                )
        );

        // Road crossing the island
        elements.add(
                new OsmWay(
                        5,
                        List.of(
                                new OsmNode(40, 100f, 700f),
                                new OsmNode(41, 200f, 600f),
                                new OsmNode(42, 300f, 500f),
                                new OsmNode(43, 400f, 400f),
                                new OsmNode(44, 500f, 300f),
                                new OsmNode(45, 600f, 200f),
                                new OsmNode(46, 700f, 100f)
                        ),
                        Map.of("highway", "path")
                )
        );

        elements.add(
                new OsmWay(
                        6,
                        List.of(
                                new OsmNode(50, 350f, 350f),
                                new OsmNode(51, 350f, 450f),
                                new OsmNode(52, 450f, 450f),
                                new OsmNode(53, 450f, 350f),
                                new OsmNode(50, 350f, 350f)
                        ),
                        Map.of("natural", "water")
                )
        );

        return elements;
    }
}
