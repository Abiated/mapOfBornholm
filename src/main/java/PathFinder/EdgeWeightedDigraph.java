package PathFinder;

import com.fasterxml.jackson.databind.JsonNode;
import interfaces.InsertableInRtree;
import models.OsmNode;
import models.OsmWay;
import parser.Parser;
import spatialDatastructures.Rtree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: Find out where to call constructGraph()
// Thoughts: Do it in main/app or wherever we run the program from. Have a check to see if the file already exists and if so read our binary file of the graph rather than construct a new one


//TODO: this class should be called in the background as soon as the program opens, to have the graph before the user clicks to make the program seem smoother


public class EdgeWeightedDigraph {
    protected int V;
    private int E;
    private List<DirectedEdge>[] adj; //main array in the adjancency list to answer "What edges go out from vertex v"
    private Rtree rtree; //TODO: why are you here?? What do you do??
    //HashMaps to go from id to index *AND* back again
    //They are here beacuse we need them AFTER the graph is built
    protected Map<Long, Integer> idToIndex;
    protected Map<Integer, Long> indexToId;
    private TagHelper tagHelper;//TODO: why are you here?? What do you do??
    //Lats and Lons are needed for the click function to work //TODO: What the fuck does this mean?


    public EdgeWeightedDigraph(int V) {
        this.V = V;
        this.E = 0;
        adj = new ArrayList[V]; //creating instance of arraylist

        for(int v = 0; v < V; v++){
            adj[v] = new ArrayList<>(); //creating instance of an arraylist in each arraylist-index
        }

        idToIndex = new HashMap<>();
        indexToId = new HashMap<>();

    }

    /** Adds a DirectedEdge to the weighted digraph used in dijkstra.
     * It is a helper method to make the program more readable.
     * Increments the number of total edges by one as E++ * */
    public void addEdge(DirectedEdge e) {
        adj[e.from()].add(e);
        E++;
    }

    /** Enables iteration through the adjacency list when used in for-loop in Dijkstra* */
    //TODO: Delete if not needed
    public Iterable<DirectedEdge> adj(int v) {
        return adj[v];
    }

    /** Returns distance between 2 nodes based on their lat and lon values using the Haversine formula* */
    public static float distance(OsmNode node1, OsmNode node2) {
        // inspiration from https://www.geeksforgeeks.org/dsa/haversine-formula-to-find-distance-between-two-points-on-a-sphere/

        //calculations made in double to keep precision and to be able to use the methods
        //get information on lat (convert from lat and lon-degrees to radians)
        //only saving lat asw e use it again i application of formula
        double lat1 = Math.toRadians(node1.getLat());
        double lat2 = Math.toRadians(node2.getLat());

        //find distance between points
        double distanceLon = Math.toRadians(node2.getLon() - node1.getLon());
        double distanceLat = Math.toRadians(lat2 - lat1);

        //application of Haversine formula
        double a = Math.pow(Math.sin(distanceLat / 2), 2) + Math.pow(Math.sin(distanceLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double earthRadius = 6371; // makes it km TODO: decide if we want km or m
        double c =  2 * Math.asin(Math.sqrt(a));

        double result =  earthRadius * c;

        return (float) result; //cast to float to match the rest of the program
    }

    /**Constructs and returns a digraph with all possible ways to travel between nodes in the map*/
    public static EdgeWeightedDigraph constructGraph(Parser input, JsonNode jRoot) {
        //TODO: Lær hvad denne her JsonNode jRoot er og gør
        //mapping long id names to array indexes
        int index = 0; //counter
        //These are different from the fields, because they are local values
        //We use them to calculate the num of V before vi instantiate EdgeWeightedDigraph
        Map<Long, Integer> idToIndex = new HashMap<>();
        Map<Integer, Long> indexToId = new HashMap<>();
        TagHelper tagHelper= new TagHelper(jRoot); //TODO: Hvorfor både jRoot & taghelper?

        for (OsmWay way : input.getOsmWayMap().values()) { //in way map
            if(!tagHelper.isHighway(way)) {continue;} //skip if OsmWay is not marked highway

            for (OsmNode node : way.getNodes()) { //in list of nodes within way
                if (!idToIndex.containsKey(node.getId())) {
                    idToIndex.put(node.getId(), index);
                    indexToId.put(index, node.getId());
                    index++;
                }
            }
        }

        //New instance
        EdgeWeightedDigraph graph = new EdgeWeightedDigraph(index);
        //fill fields with the local values
        graph.idToIndex.putAll(idToIndex);
        graph.indexToId.putAll(indexToId);
        graph.tagHelper=tagHelper;

        for (OsmWay way : input.getOsmWayMap().values()) {
            //filter relevant ways (OsmWay can ex be a building - not relevant for this graph)
            if(!tagHelper.isHighway(way)) {continue;} //skip the way if it is not highway

            List<OsmNode> nodes = way.getNodes(); //grab the list of all nodes

            for(int i = 0; i < nodes.size() - 1; i++ ) {
                OsmNode from = nodes.get(i); //set current node as from
                OsmNode to = nodes.get(i+1); //set next node as to

                //using TagtoLife
                boolean driveable = tagHelper.isDriveable(way);
                boolean walkable = tagHelper.isWalkable(way);
                boolean bikeable = tagHelper.isBikeable(way);
                float speedLimit = tagHelper.getSpeedLimit(way);

                DirectedEdge e = new DirectedEdge(
                        graph.idToIndex.get(from.getId()),
                        graph.idToIndex.get(to.getId()),
                        distance(from, to),
                        speedLimit,
                        driveable,
                        bikeable,
                        walkable
                );


                if(tagHelper.isOneWay(way)) {
                    graph.addEdge(e); //only add in this specific direction. Can be moved out and do an opposite if to add rev if != ONEWAY
                } else { //assume it goes both ways (BOTH & UNKNOWN are in this else)
                    graph.addEdge(e);

                    //create the reverse edge and add as well
                    DirectedEdge reverse = new DirectedEdge(
                            e.to(),
                            e.from(),
                            e.getLength(),
                            speedLimit,
                            driveable,
                            bikeable,
                            walkable
                    );

                    graph.addEdge(reverse);
                }
            }

        }
        List<InsertableInRtree> highwayWays = new ArrayList<>();
        for (OsmWay way : input.getOsmWayMap().values()) {
            if (tagHelper.isHighway(way)) {
                highwayWays.add(way);
            }
        }
        graph.rtree = new Rtree(highwayWays, 16);
        return graph;
    }


    //Helper methods. Might be relevant later but are currently not in use
    //TODO: decide if we need them. Else delete.
    public int indexOf(long nodeId) {
        Integer index = idToIndex.get(nodeId);
        if (index == null) {
            throw new IllegalArgumentException("Unknown node id: " + nodeId);
        }
        return index;
    }

    //to use the R-tree we are using nearest vertex
    public int nearestVertex(double lon, double lat) {
        OsmNode nearest = rtree.getNearestNeighbor((float) lat, (float) lon);
        if (nearest == null) return 0;

        Integer index = idToIndex.get(nearest.getId());
        if (index == null) return 0;
        return index;
    }

    public int nearestVertex(double lon, double lat, VehicleType vehicleType) {
        if (vehicleType == null || tagHelper == null || rtree == null) {
            return nearestVertex(lon, lat);
        }

        float[] radii = new float[]{0.0005f, 0.001f, 0.0025f, 0.005f, 0.01f, 0.025f, 0.05f};
        OsmNode bestNode = null;
        float bestDistance = Float.MAX_VALUE;

        for (float radius : radii) {
            float[] query = new float[]{
                    (float) lon - radius,
                    (float) lon + radius,
                    (float) lat - radius,
                    (float) lat + radius
            };

            List<InsertableInRtree> candidates = rtree.search(query);
            for (InsertableInRtree candidate : candidates) {
                OsmWay way = (OsmWay) candidate;
                if (!tagHelper.isAllowedForVehicle(way, vehicleType)) {
                    continue;
                }

                for (OsmNode node : way.getNodes()) {
                    float distance = distanceToPoint(node, lon, lat);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestNode = node;
                    }
                }
            }

            if (bestNode != null) {
                Integer index = idToIndex.get(bestNode.getId());
                if (index != null) {
                    return index;
                }
            }
        }

        return nearestVertex(lon, lat);
    }

    private static float distanceToPoint(OsmNode node, double lon, double lat) {
        double lat1 = Math.toRadians(lat);
        double lat2 = Math.toRadians(node.getLat());
        double distanceLon = Math.toRadians(node.getLon() - lon);
        double distanceLat = Math.toRadians(node.getLat() - lat);

        double a = Math.pow(Math.sin(distanceLat / 2), 2) + Math.pow(Math.sin(distanceLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double earthRadius = 6371;

        return (float) (earthRadius * c);
    }

    public Long nodeIdOf(int index) {
        return indexToId.get(index);
    }

    public int vertexCount() {
        return V;
    }

    public int edgeCount() {
        return E;
    }
    //getters for lats and lon
}
