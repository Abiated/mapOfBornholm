package dk.itu.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue; //standard is min oriented heap which we need for shortest path

public class Dijkstra {
    //from book p. 655
    private DirectedEdge[] edgeTo;
    private float[] distTo; //float for memory
    private PriorityQueue<Node> pq; //pq af nodes der kan indeholde
    private VehicleType vehicleType;

    public Dijkstra(EdgeWeightedDigraph G, int start, int target, VehicleType type) {
        vehicleType = type;
        edgeTo = new DirectedEdge[G.V]; //array edgeTo
        distTo = new float[G.V]; //array disTo
        pq = new PriorityQueue<Node>(G.V); //min oriented priority queue

        //initialize all distances to infinity to make them less desireable than the ones we fill with actual weights along the way
        for(int v = 0; v < G.V; v++){
            distTo[v] = Float.POSITIVE_INFINITY;
        }

        // set dist to starting vertex to 0
        distTo[start] = 0;

        //add the start Node to priority queue
        pq.add(new Node(start, 0));

        while(!pq.isEmpty()){
            Node current = pq.poll(); //keeps track of the 'head' of the priority queue
            if (current.distance > distTo[current.vertex]) continue; //ignores doublets ex if we have B(5) and B(10), then there is no reason to relax for B(10) it is a waste of time.

            if (current.vertex == target) break; //stop dijkstra's algorithm if we have reached target. No need to check shortest path to ALL other vertices when we are only interested in this one.

            relax(G, current.vertex); //relax the current head vertex in our graph
        }

    }

    //in our relax we do not update existing Nodes as Java's PriorityQueue does not support change() which is shown in the book. We instead insert a new Node. It works the same way because of the priority
    private void relax(EdgeWeightedDigraph G, int v) { //is it overkill to have vehicletype as an argument?
        for (DirectedEdge e : G.adj(v)) {
            int w = e.to;

            float weight = e.weight(e, vehicleType); //calculate the weight (currently time for car and dist for bike/walk - TODO: make it all the same. Ex. by setting an estimated walking and biking speed)

            if (distTo[w] > distTo[v] + weight) { //should .weight be a method instead to only calculate when going through this step?
                distTo[w] = distTo[v] + weight; //save the shorter/cheaper path
                edgeTo[w] = e; //remember last step to get to the vertex

                pq.add(new Node(w, distTo[w])); //insert new Node into queue regardless of whether it exists already. The priority will ensure we get a hold of the one with the shortest dist first

            }
        }
    }

    // Builds the route backwards
    public List<DirectedEdge> pathTo(int v) {
        List<DirectedEdge> path = new ArrayList<>();

        for (DirectedEdge e = edgeTo[v]; e != null; e = edgeTo[e.from]) {
            path.add(0, e);
        }

        return path;
    }

    //the following might be useful, but look into that later on
    //Standard client query methods for SPT implementations (see page 649)
    //public double distTo(int v)
    //public boolean hasPathTo(int v)
    //public Iterable<Edge> pathTo(int v)
}

