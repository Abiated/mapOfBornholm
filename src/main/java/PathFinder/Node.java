package PathFinder;

//helper class for Dijkstra when using insert() into pq as Java's PQ only takes one argument and we need both num and dist

public class Node implements Comparable<Node> {
    int vertex;
    float distance;

    public Node(int vertex, float distance) {
        this.vertex = vertex;
        this.distance = distance;
    }

    public int compareTo(Node otherNode) {
        return Float.compare(this.distance, otherNode.distance);
    }
}
