package PathFinder;

import com.fasterxml.jackson.databind.JsonNode;
import models.OsmWay;

public class TagHelper {

    private final JsonNode jRoot;

    public TagHelper(JsonNode jRoot) {
        this.jRoot = jRoot;
    }

    private JsonNode getHighwayNode(OsmWay way) {
        String value = way.getHighWayType();
        if (value == null) return null;
        JsonNode node = jRoot.path("highway").path(value);
        return node.isMissingNode() ? null : node;
    }

    public boolean isHighway(OsmWay way) {
        return way.isRoad() && getHighwayNode(way) != null;
    }

    public boolean isDriveable(OsmWay way) {
        JsonNode node = getHighwayNode(way);
        return node != null && node.path("driveable").asBoolean(false);
    }
    public boolean isOneWay(OsmWay way){
        JsonNode node= getHighwayNode(way);
        return node!= null && node.path("oneway").asBoolean(false);
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
