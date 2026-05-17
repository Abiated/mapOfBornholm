package models;

import java.io.Serial;
import java.io.Serializable;

public record Member(OsmElement element, String role) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
