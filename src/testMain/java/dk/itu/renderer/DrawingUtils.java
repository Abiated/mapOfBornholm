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
}
