package models;

import java.awt.Color;
import java.awt.geom.Path2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HeightCurve implements Serializable {
    private static final long serialVersionUID = 1L;

    long id;
    float height;
    List<Coordinate> coords;
    List<HeightCurve> children;
    boolean submerged;

    // Task 1
    public Path2D getBoundaryPath() {

        Path2D path = new Path2D.Float();

        path.moveTo(coords.getFirst().getLon(), coords.getFirst().getLat());

        for (int i = 1; i < coords.size(); i++) {
            float x = coords.get(i).getLon();
            float y = coords.get(i).getLat();
            path.lineTo(x, y);
        }

        return path;
    }

    // Task 2
    public Path2D getRegionPath() {

        Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);

        path.append(getBoundaryPath(), false);

        for (HeightCurve child : children) {
            path.append(child.getBoundaryPath(), false);
        }

        return path;
    }

    // Task 3
    public Color getFillColor(float seaLevel) {
        float elevation = height - seaLevel;

        if (elevation < 0) {
            if (submerged) return Color.decode("#2b8cbe");
            else return Color.decode("#ffffcc");
        } else if (elevation < 2.5) {
            //return Color.decode("#c2e699");
            return new Color(194, 230, 153, 0);
        } else if (elevation < 5) {
            return new Color(120, 198, 121, 0);
        } else if (elevation < 7.5) {
            return new Color(49, 163, 84, 0);
        } else {
            return new Color(0, 104, 55, 0);
        }
    }

    // Task 4
    public void resetSubmerged() {
        this.submerged = false;
        for (HeightCurve child : this.children) {
            child.resetSubmerged();
        }
    }

    // Task 5
    public void submerge(float seaLevel) {
        float thisElevation = height - seaLevel;

        if (thisElevation < 0) {
            this.submerged = true;
            for (HeightCurve child : children) {
                child.submerge(seaLevel);
            }
        }
    }

    // ------------------------------------
    // Don't edit anything below here
    // ------------------------------------
    public HeightCurve() {
        this(0L, 0.0F, new ArrayList<>(), new ArrayList<>());
    }

    public HeightCurve(long id, float height, List<Coordinate> coords) {
        this(id, height, coords, new ArrayList<>());
    }

    public HeightCurve(long id, float height, List<Coordinate> coords, List<HeightCurve> children) {
        this.id = id;
        this.height = height;
        this.coords = coords != null ? coords : new ArrayList<>();
        this.children = children != null ? children : new ArrayList<>();
        this.submerged = false;
    }

    public long getId() {
        return id;
    }

    public float getHeight() {
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
