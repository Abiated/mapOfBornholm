package dk.itu.renderer;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import static java.awt.image.ImageObserver.HEIGHT;
import java.util.Comparator;
import java.util.List;

import dk.itu.renderer.osm.OsmElement;

public class DrawingUtils {

    public static void sortOsmElementsByArea(List<OsmElement> elements) {
        // Using negative area to get biggest to smallest
        elements.sort(Comparator.comparingDouble(o -> -o.getArea()));
    }

    public static void applyTransformation(Graphics2D gc) {
        // The image spans from coordinates [0,0] to [WIDTH,HEIGHT]. Bondeholm spans a very small (0.003) area and will not be visible normally.
        // This code transforms the image by scaling and translating it such that Bondeholm comes up on the screen.

        float sf = HEIGHT / 0.003F; // Scale Factor of screen height divided by span of latitude -(maxLat - minLat).

        // Translates to the left (negative) by the left bound of Bondeholm multiplied by 0.56 to account for the earth curve projection in DK
        // Translates down (positive) by the max latitude for Bondeholm
        AffineTransform transform = AffineTransform.getTranslateInstance(-0.56 * 10.581647, 54.919275);

        // Applies transformation
        transform.preConcatenate(AffineTransform.getScaleInstance(sf, sf));
        gc.setTransform(transform);
    }
}
