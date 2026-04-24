package spatialDatastructures;

public class PQWrapper {
    private RtreeNode Node;
    private float priorityDistance;

    PQWrapper(RtreeNode e, float Distance){
        Node = e;
        priorityDistance = Distance;
    }

    public float getPriorityDistance() {return priorityDistance;}

    public RtreeNode getRtreeNode() {return Node;}
}
