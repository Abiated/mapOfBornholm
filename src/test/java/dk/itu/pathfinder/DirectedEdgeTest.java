package dk.itu.pathfinder;

import PathFinder.DirectedEdge;
import PathFinder.VehicleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DirectedEdgeTest {

    @Test
    public void weightUsesVehicleSpeedWhenVehicleIsAllowed() {
        float speedLimit = 50.0F, walkingSpeed = 4.5F, bikingSpeed = 20F;
        float length = 10.0F;
        DirectedEdge edge = new DirectedEdge(0, 1, length, speedLimit, true, true, true);

        assertEquals(length/speedLimit, edge.weight(VehicleType.CAR), 0.0001F);
        assertEquals(length/bikingSpeed, edge.weight(VehicleType.BIKE), 0.0001F);
        assertEquals(length/walkingSpeed, edge.weight(VehicleType.WALK), 0.0001F);
    }

    @Test
    public void weightIsInfiniteWhenVehicleIsNotAllowed() {
        DirectedEdge edge = new DirectedEdge(0, 1, 10.0F, 50.0F, false, true, true);

        assertTrue(Float.isInfinite(edge.weight(VehicleType.CAR)));
    }
}
