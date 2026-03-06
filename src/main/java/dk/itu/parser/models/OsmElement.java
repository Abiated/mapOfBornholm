package dk.itu.parser.models;

public abstract class OsmElement {

    private final long id;

    public OsmElement(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
