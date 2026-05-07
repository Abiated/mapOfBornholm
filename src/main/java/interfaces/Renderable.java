package interfaces;

import rendering.wayType;

public interface Renderable {
    boolean isType(wayType type);
    String getValue();
    boolean hasTag(String key, String value);
}
