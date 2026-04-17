package PathFinder;

//helper class for Dijkstra when using insert() into pq
//TODO: see of osmNode is a goo enough class to use for this or if I should keep this helper class

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
