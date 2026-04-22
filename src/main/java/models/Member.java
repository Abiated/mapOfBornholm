package models;

import java.io.Serializable;

public class Member implements Serializable {
    private static final long serialVersionUID = 1L;

    private final OsmElement element;
    private final String role;

    public Member(OsmElement element, String role) {
        this.element = element;
        this.role = role;
    }

    //Exist for deserializable as it needs a no argument constructor, value does not matter
    private Member() {
        this.element = null;
        this.role = null;
    }

    public OsmElement getElement() {
        return element;
    }

    public String getRole() {
        return role;
    }
}
