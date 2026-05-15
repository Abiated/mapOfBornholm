package dk.itu.benchmark;

import PathFinder.Dijkstra;
import PathFinder.DirectedEdge;
import PathFinder.EdgeWeightedDigraph;
import PathFinder.SubmergedVertexFinder;
import PathFinder.VehicleType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Coordinate;
import models.HeightCurve;
import models.HeightCurveData;
import models.OsmNode;
import models.OsmWay;
import parser.Parser;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The point of our benchmarking tests is to measure how long the code takes to run */
public class PathfinderBenchmark {
    private static final int[] grid_sizes = new int[]{25, 50, 100, 200}; //controlled map sizes from 25x25 to200x200
    private static final int warmup_runs = 5; //to eliminate some initial differences in runtime
    private static final int run_for_benchmarking = 30;

    public static void main(String[] args) throws Exception {
        new PathfinderBenchmark().benchmarkDijkstraAndSubmersion();
    }

    public void benchmarkDijkstraAndSubmersion() throws Exception {
        List<BenchmarkResult> results = new ArrayList<>();

        for (int grid_size : grid_sizes) { //for each loop to do the same for all grid sizes
            BenchmarkMap map = createBenchmarkMap(grid_size); //create the map

            Set<Integer> noSubmergedVertices = new HashSet<>(); //new empty hashset for when no vertices are submerged
            Set<Integer> submergedVertices = SubmergedVertexFinder.findSubmergedVertices(map.graph, map.parser, map.heightCurveData, 1.0F); //hashset for when some vertices are submerged

            int start = map.graph.indexOf(nodeId(0, 0, grid_size));
            int target = map.graph.indexOf(nodeId(grid_size - 1, grid_size - 1, grid_size));

            //print background data for clarity
            System.out.println("Pathfinder benchmark background data");
            System.out.println("Grid size: " + grid_size + " x " + grid_size);
            System.out.println("Vertices: " + map.graph.vertexCount());
            System.out.println("Submerged vertices: " + submergedVertices.size());
            System.out.println("Warmup runs: " + warmup_runs);
            System.out.println("Benchmark runs: " + run_for_benchmarking);
            System.out.println();

            //Dijkstra with and without submersion for vehicle type car
            // since all roads are allowed for all vehicle types it makes no difference here to test for different vehicle types
            long dijkstraWithoutSubmersionCar = benchmark("Dijkstra without submersion CAR", () -> {
                Dijkstra dijkstra = new Dijkstra(map.graph, start, target, VehicleType.CAR, noSubmergedVertices);
                checkPathWasFound(dijkstra, target);
            });

            long dijkstraWithSubmersionCar = benchmark("Dijkstra with submersion CAR", () -> {
                Dijkstra dijkstra = new Dijkstra(map.graph, start, target, VehicleType.CAR, submergedVertices);
                checkPathWasFound(dijkstra, target);
            });

            //SubmergedVertexFinder TODO: decide if we should somehow do more tests on this to figure out if the current way is he best (time halved when running 2-3 times)
            long submergedVertexFinder = benchmark("SubmergedVertexFinder", () -> {
                Set<Integer> result = SubmergedVertexFinder.findSubmergedVertices(
                        map.graph, map.parser, map.heightCurveData, 1.0F);
                if (result.isEmpty()) {
                    throw new IllegalStateException("Expected some submerged vertices");
                }
            });

            results.add(new BenchmarkResult(
                    grid_size,
                    map.graph.vertexCount(),
                    submergedVertices.size(),
                    dijkstraWithoutSubmersionCar,
                    dijkstraWithSubmersionCar,
                    submergedVertexFinder
            ));

            System.out.println();
        }

        printResultsTable(results);
    }

    /** Measures time for each run and returns an average
     * */
    private long benchmark(String name, BenchmarkCode code) throws Exception {
        for (int i = 0; i < warmup_runs; i++) {
            code.run();
        }

        long totalTime = 0;
        for (int i = 0; i < run_for_benchmarking; i++) {
            long startTime = System.nanoTime();
            code.run();
            long endTime = System.nanoTime();

            totalTime = totalTime + (endTime - startTime);
        }

        long averageTime = totalTime / run_for_benchmarking;
        System.out.println(name + " finished");
        return averageTime;
    }

    /** Safety check to ensure we do not get wrongly quick Dijkstra runs simply because no path is actually found
     * */
    private void checkPathWasFound(Dijkstra dijkstra, int target) {
        if (!dijkstra.hasPathTo(target)) {
            throw new IllegalStateException("Expected Dijkstra to find a path");
        }

        List<DirectedEdge> path = dijkstra.pathTo(target);
        if (path.isEmpty()) {
            throw new IllegalStateException("Expected path to contain edges");
        }
    }

    private void printResultsTable(List<BenchmarkResult> results) {
        System.out.println("Average results in milliseconds");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-10s %-10s %-10s %-20s %-20s %-20s%n", "Grid sz", "Vertices", "Subm.", "Dijkstra", "Dijkstra subm.", "Finder");

        for (BenchmarkResult result : results) {
            System.out.printf("%-10s %-10d %-10d %-20.3f %-20.3f %-20.3f%n",
                    result.gridSize + "x" + result.gridSize,
                    result.vertices,
                    result.submergedVertices,
                    toMilliseconds(result.dijkstraWithoutSubmersionCar),
                    toMilliseconds(result.dijkstraWithSubmersionCar),
                    toMilliseconds(result.submergedVertexFinder));
        }
    }

    private double toMilliseconds(long nanoseconds) {
        return nanoseconds / 1_000_000.0;
    }

    /*builds the testmap*/
    private BenchmarkMap createBenchmarkMap(int gridSize) throws Exception {
        Parser parser = new Parser(new ByteArrayInputStream(new byte[0]));

        OsmNode[][] nodes = new OsmNode[gridSize][gridSize];
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                long id = nodeId(row, col, gridSize);
                OsmNode node = new OsmNode(id, row, col);
                nodes[row][col] = node;
                parser.getOsmNodeMap().put(id, node);
            }
        }

        long wayId = 1L;
        for (int row = 0; row < gridSize; row++) {
            List<OsmNode> rowNodes = new ArrayList<>();
            for (int col = 0; col < gridSize; col++) {
                rowNodes.add(nodes[row][col]);
            }
            parser.getOsmWayMap().put(wayId, new OsmWay(wayId, rowNodes, createRoadTags()));
            wayId++;
        }

        for (int col = 0; col < gridSize; col++) {
            List<OsmNode> columnNodes = new ArrayList<>();
            for (int row = 0; row < gridSize; row++) {
                columnNodes.add(nodes[row][col]);
            }
            parser.getOsmWayMap().put(wayId, new OsmWay(wayId, columnNodes, createRoadTags()));
            wayId++;
        }

        EdgeWeightedDigraph graph = EdgeWeightedDigraph.constructGraph(parser, createConfig());
        HeightCurveData heightCurveData = createHeightCurveData(gridSize);

        return new BenchmarkMap(parser, graph, heightCurveData);
    }

    /*Makes the paths highway and residential for us to include them in our edgeweighteddigraph*/
    private HashMap<String, String> createRoadTags() {
        HashMap<String, String> tags = new HashMap<>();
        tags.put("highway", "residential");
        return tags;
    }

    /*Creates the flooded ares on the grid*/
    private HeightCurveData createHeightCurveData(int gridSize) {
        float min = gridSize * 0.2F;
        float maxLon = gridSize * 0.8F;
        float maxLat = gridSize * 0.4F;

        List<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(min, min));
        coords.add(new Coordinate(min, maxLon));
        coords.add(new Coordinate(maxLat, maxLon));
        coords.add(new Coordinate(maxLat, min));
        coords.add(new Coordinate(min, min));

        HeightCurve sea = new HeightCurve(0L, -1.0F, new ArrayList<>(), new ArrayList<>());
        HeightCurve lowArea = new HeightCurve(1L, 0.0F, coords, new ArrayList<>());
        sea.getChildren().add(lowArea);

        List<HeightCurve> curves = new ArrayList<>();
        curves.add(sea);
        curves.add(lowArea);

        return new HeightCurveData(0.0F, 0.0F, gridSize, gridSize, sea, curves);
    }

    /*the json config needed to run constructGraph from EdgeWeightedDigraph*/
    private JsonNode createConfig() throws Exception {
        String json = "{ \"highway\": { \"residential\": { \"speed\": 50, " +
                "\"driveable\": true, \"cyclable\": true, \"walkable\": true } } }";
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(json.getBytes(StandardCharsets.UTF_8));
    }

    /*Provides each node in our artificial test map an ID*/
    private static long nodeId(int row, int col, int gridSize) {
        return row * gridSize + col + 1;
    }

    private interface BenchmarkCode {
        void run() throws Exception;
    }

    /*Collects parser, graph & height curve data to send it colleted*/
    private record BenchmarkMap(Parser parser, EdgeWeightedDigraph graph, HeightCurveData heightCurveData) {
    }

    private record BenchmarkResult(
            int gridSize,
            int vertices,
            int submergedVertices,
            long dijkstraWithoutSubmersionCar,
            long dijkstraWithSubmersionCar,
            long submergedVertexFinder) {
    }
}
