package PathFinder;

import com.fasterxml.jackson.databind.JsonNode;
import models.OsmWay;
import rendering.wayType;

public class TagHelper {

    private final JsonNode jRoot;

    public TagHelper(JsonNode jRoot) {
        this.jRoot = jRoot;
    }

    private JsonNode getHighwayNode(OsmWay way) {
        String value = way.getValue();
        if (value == null) return null;
        JsonNode node = jRoot.path("highway").path(value);
        return node.isMissingNode() ? null : node;
    }

    public boolean isHighway(OsmWay way) {
        return way.getType(wayType.ROAD) && getHighwayNode(way) != null;
    }

    public boolean isDriveable(OsmWay way) {
        JsonNode node = getHighwayNode(way);
        return node != null && node.path("driveable").asBoolean(false);
    }
    public boolean isOneWay(OsmWay way){
        String oneway = way.getTags().get("oneway");
        if (oneway == null) {
            return false;
        }

        oneway = oneway.trim().toLowerCase();
        return oneway.equals("yes") || oneway.equals("true") || oneway.equals("1");
    }

    public boolean isBikeable(OsmWay way) {
        JsonNode node = getHighwayNode(way);
        return node != null && node.path("cyclable").asBoolean(false);
    }

    public boolean isWalkable(OsmWay way) {
        JsonNode node = getHighwayNode(way);
        return node != null && node.path("walkable").asBoolean(false);
    }

    public float getSpeedLimit(OsmWay way) {
        String maxSpeed = way.getTags().get("maxspeed");
        if (maxSpeed != null) {
            String normalized = maxSpeed.trim().toLowerCase();
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("(\\d+(?:[.,]\\d+)?)")
                    .matcher(normalized);

            if (matcher.find()) {
                float parsedSpeed = Float.parseFloat(matcher.group(1).replace(',', '.'));
                if (normalized.contains("mph")) {
                    parsedSpeed *= 1.60934f;
                }
                return parsedSpeed;
            }
        }

        JsonNode node = getHighwayNode(way);
        if (node == null) return 50f;
        return (float) node.path("speed").asDouble(50.0);
    }

    public boolean isAllowedForVehicle(OsmWay way, VehicleType vehicleType) {
        return switch (vehicleType) {
            case CAR  -> isDriveable(way);
            case BIKE -> isBikeable(way);
            case WALK -> isWalkable(way);
        };
    }
}
