package models;

import java.io.Serializable;

public abstract class OsmElement implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long id;
    private float area;

    public OsmElement(long id) {
        this.id = id;
    }
    private OsmElement() {this.id = 0;}

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
