package dk.itu.renderer.osm;

import dk.itu.renderer.Drawable;

public abstract class OsmElement implements Drawable {

    private final long id;
    private double area;

    public OsmElement(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public double getArea() {
        return this.area;
    }

    protected void setArea(double area) {
        this.area = area;
    }
}
