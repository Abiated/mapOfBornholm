package dk.itu.Model;

import TagHelper.TagtoLife;
import dk.itu.parser.Parser;
import dk.itu.renderer.osm.OsmNode;
import dk.itu.renderer.osm.OsmWay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DijkstraDemo {

    public static void main(String[] args) {
        String fileName = args.length > 0 ? args[0] : "hc-osm-files/samso/samso.osm";
        VehicleType vehicleType = parseVehicleType(args);

        Parser parser = new Parser(fileName);
        parser.parse();

        EdgeWeightedDigraph graph = new EdgeWeightedDigraph(0).constructGraph(parser);
        OsmWay demoWay = findDemoWay(parser, vehicleType);

        if (demoWay == null) {
            System.out.println("Could not find a suitable highway for " + vehicleType);
            return;
        }

        int startPosition = parseNodePosition(args, 2, 0, demoWay.getNodes().size() - 1);
        int targetPosition = parseNodePosition(args, 3, demoWay.getNodes().size() - 1, demoWay.getNodes().size() - 1);

        //choose real map nodes from the selected way
        OsmNode startNode = demoWay.getNodes().get(startPosition);
        OsmNode targetNode = demoWay.getNodes().get(targetPosition);

        //read the node's id and get the index to use it in route
        int startIndex = graph.indexOf(startNode.getId());
        int targetIndex = graph.indexOf(targetNode.getId());

//        int startIndex = graph.indexOf(chosenStartId); //try and play around with these. Remember ID is the NodeID from osm data
//        int targetIndex = graph.indexOf(chosenTargetId);

        //run Dijkstra on the start and target node with the specified vehicle type
        Dijkstra dijkstra = new Dijkstra(graph, startIndex, targetIndex, vehicleType);

        System.out.println("File: " + fileName);
        System.out.println("Vehicle: " + vehicleType);
        System.out.println("Demo way node count: " + demoWay.getNodes().size());
        System.out.println("Start position inside chosen way: " + startPosition);
        System.out.println("Target position inside chosen way: " + targetPosition);
        System.out.println("Vertices in graph: " + graph.vertexCount());
        System.out.println("Edges in graph: " + graph.edgeCount());
        System.out.println("Start node id: " + startNode.getId() + " -> index " + startIndex);
        System.out.println("Target node id: " + targetNode.getId() + " -> index " + targetIndex);

        if (!dijkstra.hasPathTo(targetIndex)) {
            System.out.println("No path found.");
            return;
        }

        List<DirectedEdge> path = dijkstra.pathTo(targetIndex);
        float totalLength = 0.0f;

        System.out.println("Path found.");
        System.out.println("Number of directed edges in path: " + path.size());
        System.out.println("Total route cost: " + dijkstra.distTo(targetIndex));

        for (DirectedEdge edge : path) {
            totalLength += edge.getLength();
        }
        System.out.println("Total geometric length in km: " + totalLength);

        int limit = Math.min(path.size(), 10);
        System.out.println("First " + limit + " edges in the route:");
        for (int i = 0; i < limit; i++) {
            DirectedEdge edge = path.get(i);
            System.out.println(
                    "  " + (i + 1) +
                    ": " + graph.nodeIdOf(edge.from()) +
                    " -> " + graph.nodeIdOf(edge.to()) +
                    " | length=" + edge.getLength()
            );
        }
    }

    private static OsmWay findDemoWay(Parser parser, VehicleType vehicleType) {
        List<OsmWay> highways = new ArrayList<>();

        for (OsmWay way : parser.getOsmWayMap().values()) {
            if (!TagtoLife.isHighway(way)) {
                continue;
            }
            if (way.getNodes().size() < 2) {
                continue;
            }
            if (!isAllowedForVehicle(way, vehicleType)) {
                continue;
            }
            highways.add(way);
        }

        highways.sort(Comparator.comparingInt((OsmWay way) -> way.getNodes().size()).reversed());

        return highways.isEmpty() ? null : highways.get(0);
    }

    private static boolean isAllowedForVehicle(OsmWay way, VehicleType vehicleType) {
        return switch (vehicleType) {
            case CAR -> TagtoLife.isDriveable(way);
            case BIKE -> TagtoLife.isBikeable(way);
            case WALK -> TagtoLife.isWalkable(way);
        };
    }

    private static VehicleType parseVehicleType(String[] args) {
        if (args.length < 2 || args[1] == null || args[1].isBlank()) {
            return VehicleType.CAR;
        }

        try {
            return VehicleType.valueOf(args[1].trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown vehicle type '" + args[1] + "'. Defaulting to CAR.");
            return VehicleType.CAR;
        }
    }

    private static int parseNodePosition(String[] args, int argIndex, int defaultValue, int maxValue) {
        if (args.length <= argIndex || args[argIndex] == null || args[argIndex].isBlank()) {
            return defaultValue;
        }

        try {
            int parsed = Integer.parseInt(args[argIndex].trim());
            if (parsed < 0 || parsed > maxValue) {
                System.out.println("Node position " + parsed + " is outside 0.." + maxValue + ". Using default " + defaultValue + ".");
                return defaultValue;
            }
            return parsed;
        } catch (NumberFormatException e) {
            System.out.println("Could not parse node position '" + args[argIndex] + "'. Using default " + defaultValue + ".");
            return defaultValue;
        }
    }
}
