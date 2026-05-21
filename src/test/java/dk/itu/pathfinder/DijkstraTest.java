package dk.itu.pathfinder;

import PathFinder.Dijkstra;
import PathFinder.DirectedEdge;
import PathFinder.EdgeWeightedDigraph;
import PathFinder.VehicleType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DijkstraTest {

    // TODO: consider taking some of the prep for each test out and put in a @beforeEach
    // TODO: More comments to make it easily readable for exam prep
    // TODO: perhaps we should test for another vehicle type than car

    @Test
    public void dijkstraChoosesShortestPathWhenThereIsNoSubmersion() {
        EdgeWeightedDigraph graph = createGraphWithTwoPossibleRoutes();
        Set<Integer> submergedVertices = new HashSet<>();

        Dijkstra dijkstra = new Dijkstra(graph, 0, 3, VehicleType.CAR, submergedVertices);
        List<DirectedEdge> path = dijkstra.pathTo(3);

        assertTrue(dijkstra.hasPathTo(3));
        assertEquals(2, path.size());
        assertEquals(1, path.get(0).to());
        assertEquals(3, path.get(1).to());
        assertEquals(routeWeight(path, VehicleType.CAR), dijkstra.distTo(3), 0.0F);
    }

    @Test
    public void dijkstraAvoidsSubmergedVerticesWhenAnotherRouteExists() {
        EdgeWeightedDigraph graph = createGraphWithTwoPossibleRoutes();
        Set<Integer> submergedVertices = new HashSet<>();
        submergedVertices.add(1);

        Dijkstra dijkstra = new Dijkstra(graph, 0, 3, VehicleType.CAR, submergedVertices);
        List<DirectedEdge> path = dijkstra.pathTo(3);

        assertTrue(dijkstra.hasPathTo(3));
        assertEquals(2, path.size());
        assertEquals(2, path.get(0).to());
        assertEquals(3, path.get(1).to());
        assertEquals(routeWeight(path, VehicleType.CAR), dijkstra.distTo(3), 0.0F);
    }

    @Test
    public void dijkstraFindsNoPathWhenStartOrTargetIsSubmerged() {
        EdgeWeightedDigraph graph = createGraphWithTwoPossibleRoutes();

        Set<Integer> submergedStart = new HashSet<Integer>();
        submergedStart.add(0);
        Dijkstra dijkstraFromSubmergedStart = new Dijkstra(graph, 0, 3, VehicleType.CAR, submergedStart);
        assertFalse(dijkstraFromSubmergedStart.hasPathTo(3));

        Set<Integer> submergedTarget = new HashSet<Integer>();
        submergedTarget.add(3);
        Dijkstra dijkstraToSubmergedTarget = new Dijkstra(graph, 0, 3, VehicleType.CAR, submergedTarget);
        assertFalse(dijkstraToSubmergedTarget.hasPathTo(3));
    }

    // helper which creates a controlled EdgeWeightedDigraph for our tests
    private EdgeWeightedDigraph createGraphWithTwoPossibleRoutes() {
        EdgeWeightedDigraph graph = new EdgeWeightedDigraph(4);

        graph.addEdge(new DirectedEdge(0, 1, 1.0F, 50.0F, true, true, true));
        graph.addEdge(new DirectedEdge(1, 3, 1.0F, 50.0F, true, true, true));

        graph.addEdge(new DirectedEdge(0, 2, 5.0F, 50.0F, true, true, true));
        graph.addEdge(new DirectedEdge(2, 3, 5.0F, 50.0F, true, true, true));

        return graph;
    }

    private float routeWeight(List<DirectedEdge> path, VehicleType vehicleType) {
        float totalWeight = 0.0F;
        for (DirectedEdge edge : path) {
            totalWeight = totalWeight + edge.weight(vehicleType);
        }
        return totalWeight;
    }
}
