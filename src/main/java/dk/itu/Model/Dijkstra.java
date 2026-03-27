package main.java.dk.itu.Model;

import java.util.PriorityQueue; //standard is min oriented heap which we need for shortest path

public class Dijkstra {
//from book p. 655
    private DirectedEdge[] edgeTo;
    private float[] distTo; //should it be float for memory?
    private PriorityQueue<Float> pq; //should double be float for memory?

    //TA: PQ på Nodes og sætter startnode ind.

    public Dijkstra(EdgeWeightedDigraph G, int start) {
        edgeTo = new DirectedEdge[G.V()]; //array edgeTo
        distTo = new float[G.V()]; //array disTo
        pq = new PriorityQueue<Float>(G.V()); //min oriented priority queue

        //initialize all distances to infinity
        for(int v = 0; v < G.V(); v++){
            distTo[v] = Float.POSITIVE_INFINITY;
        }

        // set dist to starting vertex to 0
        distTo[start] = 0;

        pq.insert(start, 0.0);
        while(!pq.isEmpty()){
            relax(G, pq.delMin());
        }

    }
    private void relax(EdgeWeightedDigraph G, int v){
        for(DirectedEdge e: G.adj(v)){
            int w = e.to;
            if(distTo[w] > distTo[v] + e.weight){ //should .weight be a methos instead to only calculate when going through this step?
                distTo[w] = distTo[v] + e.weight; //save the shorter/cheaper path
                edgeTo[w] = e; //remember last step to get to the vertex

                //if it is already in queue update it
                if(pq.contains(w)){pq.change(w, distTo[w]);}
                else{pq.insert(w,distTo[w]);} //else insert into queue
            }
        }

        //the following might be useful, but look into that later on
        //Standard client query methods for SPT implementations (see page 649)
        //public double distTo(int v)
        //public boolean hasPathTo(int v)
        //public Iterable<Edge> pathTo(int v)
    }









    /* Notes:
    Single source shortest path:
    Input: directed, weighted graph G consisting of vertices v, edges e and weights c
    The task: for a given stating vertex find the shortest path to every other vertex

    Dijkstra
    Has array distTo and edgeTo
    distTo stores the length (weights counted together) of the shortest path from starting v to the vertex in position index position i
    edgeTo stores the very last edge along the shortest path leading from starting vertex to the vertex in index i

    Basic operation
    relax(v, w, c){ //v is a vertex, w is a vertex
    if(distTo[w] > distTo[v] + c(v+w)){
        distTo[w] = distTo[v] + c(v+w) //we want to keep the shorter one in this case distTo[v] + c(v+w)
        edgeTo[w] = v //the last vertex before arriving at w is v
    }
    }

    //the algorithm should take parameters
    dijkstra(vehicle_type, fastest, ...)

    djikstra(graph, start, c){
        initialize(graph, start)
        set S = set V;
        Q = start;
        while(!Q.isEmpty()){
        u = findMin(distTo);
        S.add(u) // add u to the set S
        For each v such that (u,v) is an edge{
            relax(u, v, c)}
        Delete vertex u from Q
        }
    }

   We have to build graph G ourselves:
   The straight forward way would be to have an edge for each segment of a way
   The improved way would be only splitting edges at intersections
   Make sure the graph is only stored once
   Do NOT construct separate graphs for different route types (ie car, bike and walk should be findable through the same graph)
   Annotate each edge ex by length, one way or speed limit

   Regarding weights:
   Car routes should take into account speed limits/expected average speeds
   Biking/walking should be shortest possible routes and they cannot use highways

    Can be static (lenght and speed) which we intend to do. But they could also be dynamic (traffic, congestion or turn delays) if we collected even more data, Perhaps use this in the rapport

   Some tips
   In our routing application we only need the shortest path between 1 pair of nodes. Therefore, we must stop Dijkstra as soon as we reach that node. Notes from medium.com: "once the destination node is popped from the queue, you can stop if you only care about a single target."
   Use weights on the edges to model the fastest/shortest route
   Check if the edge can be relaxed for the current vehicle type BEFORE relaxing it.

    Thoughts:
    Use a priority queue in the form of a min oriented heap.

    I was wondering:
    How do I ensure no negative edges?
    How to I make sure one way roads stay one way when the graph is undirected??
    *
    * */





}
