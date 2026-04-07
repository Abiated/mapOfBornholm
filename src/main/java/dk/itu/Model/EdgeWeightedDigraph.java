package dk.itu.Model;

import TagHelper.Direction;
import TagHelper.TagtoLife;
import dk.itu.parser.Parser;
import dk.itu.renderer.osm.OsmNode;
import dk.itu.renderer.osm.OsmWay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: Find out where to call constructGraph()
// Thoughts: Do it in main/app or wherever we run the program from. Have a check to see if the file already exists and if so read our binary file of the graph rather than construct a new one

public class EdgeWeightedDigraph {
    protected int V; //vertices. is originally set as final in Sedgewick & Wayne but compiler does not like. How to deal with that
    private int E; //edges
    private List<DirectedEdge>[] adj; //array in adjancency list to answer "What edges go out from vertex v"

    //Should we have HashMaps to go from id to index *AND* back again??
    protected Map<Long, Integer> idToIndex;

    public EdgeWeightedDigraph(int V) {
        this.V = V;
        this.E = 0;
        adj = new ArrayList[V]; //why, what, hvorfor?? Hvad sker der in linjerne her i bogen overhovedet??

        for(int v = 0; v < V; v++){
            adj[v] = new ArrayList(); //her laver vi arrayList der er i hvert index i array
        }

        idToIndex = new HashMap<>();
    }

    /** Adds a DirectedEdge to the weighted digraph used in dijkstra.
     * Increments the number of total edges by one as E++ * */
    public void addEdge(DirectedEdge e) {
        adj[e.from].add(e); //was originally from(). Read this line again and make sure you fully understand what is happening: we look into the array at index edge e's 'from' field. We then add edge e to?? //TODO: do we use fields directly or get methods
        E++;
    }

    /** Enables iteration through the adjacencylist when used in for loop in Dijkstra* */
    public Iterable<DirectedEdge> adj(int v) {
        return adj[v];
    }

    /** Returns distance between 2 nodes based on their lat and lon values using the Haversine formula* */
    public static float distance(OsmNode node1, OsmNode node2) {
        // inspiration from https://www.geeksforgeeks.org/dsa/haversine-formula-to-find-distance-between-two-points-on-a-sphere/

        //calculations made in double to keep precision and to be able to use the methods
        //get information on lat (convert from lat and lon-degrees to radians)
        //only saving lat
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
    public EdgeWeightedDigraph constructGraph(Parser input) {

        //mapping long id names to array indexes
        int index = 0;
        for (Long id : input.getOsmNodeMap().keySet()) {
            idToIndex.put(id, index++);
        }

        //create graph
        EdgeWeightedDigraph graph = new EdgeWeightedDigraph(idToIndex.size());
        for (OsmWay way : input.getOsmWayMap().values()) {
            //filter relevant ways (OsmWay can ex be a building - not relevant for this graph)
            if(!TagtoLife.isHighway(way)) {continue;} //skip the way if it is not highway

            List<OsmNode> nodes = way.getNodes(); //grab the list of all nodes

            for(int i = 0; i < nodes.size() - 1; i++ ) {
                OsmNode from = nodes.get(i);
                OsmNode to = nodes.get(i+1);

                DirectedEdge e = new DirectedEdge();
                e.from = idToIndex.get(from.getId());
                e.to = idToIndex.get(to.getId());
                e.length = distance(from, to); //create distance method to calculate distance between nodes

                //using TagtoLife
                e.driveable = TagtoLife.isDriveable(way);
                e.walkable = TagtoLife.isWalkable(way);
                e.bikeable = true; //should be same format as the others if we decide to keep bikeable. For now all roads are bikeable
                //possible enhancement for bikeable
                // e.bikeable = TagtoLife.getTransportationType(way) != MOTORWAY;

                Direction direction = TagtoLife.getDirection(way); //create method or find another way to access this information

                if(direction == Direction.ONEWAY) {
                    graph.addEdge(e); //only add in this specific direction. Can be moved out and do an opposite if to add rev if != ONEWAY
                } else { //assume it goes both ways (BOTH & UNKNOWN are in this else)
                    graph.addEdge(e);

                    //create the reverse edge and add as well
                    DirectedEdge reverse = new DirectedEdge();
                    reverse.from = e.to;
                    reverse.to = e.from;
                    reverse.length = e.length;

                    reverse.driveable = TagtoLife.isDriveable(way);
                    reverse.walkable = TagtoLife.isWalkable(way);
                    reverse.bikeable = true; //TODO: update when we have decided if we want this information

                    graph.addEdge(reverse);
                }
            }

        }
        return graph;
    }




}
