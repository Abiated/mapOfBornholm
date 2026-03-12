package dk.itu.renderer.osm;

public class Member {

    private final OsmElement element;
    private final String role;

    public Member(OsmElement element, String role) {
        this.element = element;
        this.role = role;
    }

    public OsmElement getElement() {
        return element;
    }

    public String getRole() {
        return role;
    }
}
