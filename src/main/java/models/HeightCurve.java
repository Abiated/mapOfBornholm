package models;

import interfaces.InsertableInRtree;

import java.awt.*;
import java.awt.geom.Path2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HeightCurve implements Serializable, InsertableInRtree {
    private static final long serialVersionUID = 1L;

    long id;
    float height;
    List<Coordinate> coords;
    List<HeightCurve> children;
    boolean submerged;
    float[] boundingBox;
    Shape cachedRegionPath;
    Shape cachedBoundaryPath;

    // Task 1
    public Path2D getBoundaryPath() {

        Path2D path = new Path2D.Float();

        path.moveTo(coords.getFirst().getLon(), coords.getFirst().getLat());

        for (int i = 1; i < coords.size(); i+=20) {
            float x = coords.get(i).getLon();
            float y = coords.get(i).getLat();
            if(i == coords.size()-20) {
                int minus = 20 - (coords.size() % 20);
                i -= minus;
            }
            path.lineTo(x, y);
        }

        setCachedBoundaryPath(path);
        return path;
    }

    // Task 2
    public Path2D getRegionPath() {

        Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);

        Shape boundaryPath;
        if (getCachedBoundaryPath()==null) boundaryPath = getBoundaryPath();
        else boundaryPath = getCachedBoundaryPath();
        path.append(boundaryPath, false);

        for (HeightCurve child : children) {
            if (child.getCachedBoundaryPath()==null)
                path.append(child.getBoundaryPath(), false);
            else
                path.append(child.getCachedBoundaryPath(), false);
        }

        setCachedRegionPath(path);
        return path;
    }

    // Task 3
    public Color getFillColor(float seaLevel, boolean visible) {
        float elevation = height - seaLevel;
        if (visible) {
            if (elevation < 0) {
                if (submerged) return Color.decode("#2b8cbe");
                else return Color.decode("#ffffcc");
            } else if (elevation < 2.5) {
                //return Color.decode("#c2e699");
                return new Color(194, 230, 153, 255);
            } else if (elevation < 5) {
                return new Color(120, 198, 121, 255);
            } else if (elevation < 7.5) {
                return new Color(49, 163, 84, 255);
            } else {
                return new Color(0, 104, 55, 255);
            }
        } else {
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

    public float[] getBoundingBox() {
        if (boundingBox == null) return new float[]{0, 0, 0, 0};
        return boundingBox;
    }

    public boolean intersects(float[] otherBbox) {
        return !(boundingBox[1] < otherBbox[0] ||
                boundingBox[0] > otherBbox[1] ||
                boundingBox[3] < otherBbox[2] ||
                boundingBox[2] > otherBbox[3]);
    }

    public void setCachedRegionPath(Shape shape) {
        cachedRegionPath = shape;
    }

    public Shape getCachedRegionPath() { return cachedRegionPath; }

    public void setCachedBoundaryPath(Shape shape) {
        cachedBoundaryPath = shape;
    }

    public Shape getCachedBoundaryPath() { return cachedBoundaryPath; }

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
        if (!this.coords.isEmpty()) {
            float minLon = Float.MAX_VALUE;
            float maxLon = 0;
            float minLat = Float.MAX_VALUE;
            float maxLat = 0;

            for (Coordinate coord : this.coords) {
                if (coord.getLat() > maxLat){
                    maxLat = coord.getLat();
                }
                if (coord.getLat() < minLat){
                    minLat = coord.getLat();
                }
                if (coord.getLon() > maxLon){
                    maxLon = coord.getLon();
                }
                if (coord.getLon() < minLon){
                    minLon = coord.getLon();
                }
            }
            boundingBox = new float[] {minLon,maxLon,minLat,maxLat};
        }
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
