package main.java.dk.itu.Model;


import java.util.ArrayList;
import java.util.List;

public class EdgeWeightedDigraph {
    protected int V; //vertices. is originally set as final in Sedgewick & Wayne but compiler does not lke. How to deal with that
    private int E; //edges
    private List<DirectedEdge>[] adj; //array in adjancency list to answer "What edges go out from vertex v"

    public EdgeWeightedDigraph(int V) {
        this.V = V;
        this.E = 0;
        adj = new ArrayList[V]; //why, what, hvorfor?? Hvad sker der in linjerne her i bogen overhovedet??

        for(int v = 0; v < V; v++){
            adj[v] = new ArrayList(); //her laver vi arrayList der er i hvert index i array
        }

    }

    /** Adds a DirectedEdge to the weighted digraph used in dijkstra.
     * Increments the number of total edges by one as E++ * */
    public void addEdge(DirectedEdge e) {
        adj[e.from].add(e); //was originally from(). Read this line again and make sure you fully understand what is happening: we look into the array at index edge e's 'from' field. We then add edge e to?? //TODO: do we use fields directly or get methods
        E++;
    }

    /** Enables iteration through the adjacencylist when used in for loop in Dijkstra
     *
     * */
    public Iterable<DirectedEdge> adj(int v) {
        return adj[v];
    }






}
