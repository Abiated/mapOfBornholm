package dk.itu.pathfinder;

import PathFinder.DirectedEdge;
import PathFinder.VehicleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DirectedEdgeTest {

    @Test
    public void weightUsesVehicleSpeedWhenVehicleIsAllowed() {
        DirectedEdge edge = new DirectedEdge(0, 1, 10.0F, 50.0F, true, true, true);

        assertEquals(0.2F, edge.weight(VehicleType.CAR), 0.0001F);
        assertEquals(0.5F, edge.weight(VehicleType.BIKE), 0.0001F);
        assertEquals(2.0F, edge.weight(VehicleType.WALK), 0.0001F);
    }

    @Test
    public void weightIsInfiniteWhenVehicleIsNotAllowed() {
        DirectedEdge edge = new DirectedEdge(0, 1, 10.0F, 50.0F, false, true, true);

        assertTrue(Float.isInfinite(edge.weight(VehicleType.CAR)));
    }
}
