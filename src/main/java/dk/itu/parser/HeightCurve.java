package dk.itu.parser;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class HeightCurve {

    long id;
    double height;
    List<Coordinate> coords;
    List<HeightCurve> children;
    boolean submerged;

    // Task 1
    public Path2D getBoundaryPath() {
        Path2D line = new Path2D.Double();
        line.moveTo(coords.getFirst().getLon(),coords.getFirst().getLat());
        for (int i=1; i<coords.size(); i++)
            line.lineTo(coords.get(i).getLon(),coords.get(i).getLat());
        return line;
    }
    // Task 2
    public Path2D getRegionPath() {
        Path2D region = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        region.append(getBoundaryPath(),false);
        for (HeightCurve hc : children)
            region.append(hc.getBoundaryPath(),false);
        return region;
    }

    // Task 3
    public Color getFillColor(double seaLevel) {
        // Placeholder color until Task 3 is implemented.
        // Semi-transparent so overlapping regions are visually debuggable.
        double elevation = height - seaLevel;


        if (elevation < 0 && submerged)
            return Color.decode("#2b8cbe");
        else
            return null;
        /*
        else if (elevation < 0)
            return Color.decode("#ffffcc");
        else if (elevation >= 0 && elevation < 2.5)
            return Color.decode("#c2e699");
        else if (elevation >= 2.5 && elevation < 5)
            return Color.decode("#78c679");
        else if (elevation >= 5 && elevation < 7.5)
            return Color.decode("#31a354");
        else
            return Color.decode("#006837");

         */

    }

    // Task 4
    public void resetSubmerged() {
        this.submerged = false;
        for (HeightCurve child : this.children) {
            child.resetSubmerged();
        }
    }

    // Task 5
    public void submerge(double seaLevel) {
        if (height-seaLevel<0)
            this.submerged = true;
        for (HeightCurve child : this.children) {
            if (child.getHeight()-seaLevel<0)
                child.submerge(seaLevel);
        }
    }

    // ------------------------------------
    // Don't edit anything below here
    // ------------------------------------
    public HeightCurve() {
        this(0L, 0.0, new ArrayList<>(), new ArrayList<>());
    }

    public HeightCurve(long id, double height, List<Coordinate> coords) {
        this(id, height, coords, new ArrayList<>());
    }

    public HeightCurve(long id, double height, List<Coordinate> coords, List<HeightCurve> children) {
        this.id = id;
        this.height = height;
        this.coords = coords != null ? coords : new ArrayList<>();
        this.children = children != null ? children : new ArrayList<>();
        this.submerged = false;
    }

    public long getId() {
        return id;
    }

    public double getHeight() {
        return height;
    }

    public List<Coordinate> getCoords() {
        return coords;
    }

    public List<HeightCurve> getChildren() {
        return children;
    }

    public boolean isSubmerged() {
        return submerged;
    }
}
