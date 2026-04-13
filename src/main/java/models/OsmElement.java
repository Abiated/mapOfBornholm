package models;

public abstract class OsmElement {

    private final long id;
    private float area;

    public OsmElement(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public float getArea() {
        return this.area;
    }

    protected void setArea(float area) {
        this.area = area;
    }
}
