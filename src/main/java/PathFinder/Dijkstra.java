package PathFinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue; //standard is min oriented heap which we need for shortest path
import java.util.Set;

public class Dijkstra {
    private DirectedEdge[] edgeTo;
    private float[] distTo;
    private PriorityQueue<Node> pq;
    private VehicleType vehicleType;
    private Set<Integer> submergedVertices;

    // overloaded constructor which sends information on to Dijkstra.
    //it creates a new empty hashset to pass on to Dijkstra. In case no vertices are submerged it uses the empty hashset. Perhaps we can find a more readable way to do this
    public Dijkstra(EdgeWeightedDigraph G, int start, int target, VehicleType type) {
        //calls the other constructor for Dijkstra
        this(G, start, target, type, new HashSet<>());
    }

    public Dijkstra(EdgeWeightedDigraph G, int start, int target, VehicleType type, Set<Integer> submergedVertices) {
        vehicleType = type;
        this.submergedVertices = submergedVertices != null ? submergedVertices : new HashSet<>();
        edgeTo = new DirectedEdge[G.V]; //array edgeTo
        distTo = new float[G.V]; //array disTo
        pq = new PriorityQueue<>(G.V); //min oriented priority queue

        //initialize all distances to positive infinity to make them less desireable than the ones we fill with actual weights along the way
        for(int v = 0; v < G.V; v++){
            distTo[v] = Float.POSITIVE_INFINITY;
        }

        if (this.submergedVertices.contains(start) || this.submergedVertices.contains(target)) {
            return;
        }

        // set distance to starting vertex to 0
        distTo[start] = 0;

        //add the start Node to priority queue
        pq.add(new Node(start, 0));

        while(!pq.isEmpty()){


            Node current = pq.poll(); //keeps track of the 'head' of the priority queue
            if (current.distance > distTo[current.vertex]) continue; //ignores doublets ex if we have B(5) and B(10), then there is no reason to relax for B(10) it is a waste of time.

            if (current.vertex == target) break; //stop dijkstra's algorithm if we have reached target. No need to check shortest path to ALL other vertices when we are only interested in this one.

            relax(G, current.vertex); //relax the current head vertex in our graph
        }

        //Print for visual test:
        System.out.println("Dijkstra ran till the end");
    }

    //in our relax we do not update existing Nodes as Java's PriorityQueue does not support change() which is shown in the book. We instead insert a new Node. It works the same way because of the priority
    private void relax(EdgeWeightedDigraph G, int v) {
        if(submergedVertices.contains(v)){return;} //if the current vertex is submerged do not relax edge. Distance remains infinity and Dijkstra looks to other vertices and their edges

        for (DirectedEdge e : G.adj(v)) {

            int w = e.to();
            if(submergedVertices.contains(w)){continue;} //if the current neighbor to v, w, is submerged skip this point in relaxation and look to other neighbor vertices instead. Do not return or else we miss out on potentially not submerged neighbors

            float weight = e.weight(vehicleType); //calculate the weight (currently time for car, bike & walk

            if (distTo[w] > distTo[v] + weight) {
                distTo[w] = distTo[v] + weight; //save the shorter/cheaper path
                edgeTo[w] = e; //remember last step to get to the vertex

                pq.add(new Node(w, distTo[w])); //insert new Node into queue regardless of whether it exists already. The priority will ensure we get a hold of the one with the shortest dist first

            }
        }
    }

    // Builds the route backwards
    public List<DirectedEdge> pathTo(int v) {
        List<DirectedEdge> path = new ArrayList<>();

        for (DirectedEdge e = edgeTo[v]; e != null; e = edgeTo[e.from()]) {
            path.add(0, e);
        }

        return path;
    }

    public boolean hasPathTo(int v) {
        return distTo[v] < Float.POSITIVE_INFINITY;
    }

    public float distTo(int v) {
        return distTo[v];
    }


}

