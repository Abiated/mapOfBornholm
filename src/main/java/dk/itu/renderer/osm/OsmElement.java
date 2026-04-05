package dk.itu.renderer.osm;

public abstract class OsmElement {

    private final long id;
    private float area;

    public OsmElement(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public double getArea() {
        return this.area;
    }

    protected void setArea(float area) {
        this.area = area;
    }
}
