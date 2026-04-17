package dk.itu.Model;

public class DirectedEdge {
    private final int from;
    private final int to;
    private final float length;
    private final float speedLimit;
    private final boolean driveable;
    private final boolean bikeable;
    private final boolean walkable;

    public DirectedEdge(int from, int to, float length, float speedLimit,
                        boolean driveable, boolean bikeable, boolean walkable) {
        this.from = from;
        this.to = to;
        this.length = length;
        this.speedLimit = speedLimit;
        this.driveable = driveable;
        this.bikeable = bikeable;
        this.walkable = walkable;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public float getLength() {
        return length;
    }

    public float weight(VehicleType type){
        //if the vehicle type is not allowed to traverse an edge set the distance to positive infinity
        // any other allowed edge will thus be shorter and this edge is not in the final shortest path
        if(!isAllowed(type)){return Float.POSITIVE_INFINITY;}

        if(type == VehicleType.CAR){return length / speedLimit;} //gives the time it takes to traverse for the car
        else{return length;} //gives the distance for bike and walk
    }

    private boolean isAllowed(VehicleType type){
        if(driveable && type == VehicleType.CAR) {return true;}
        if(bikeable && type == VehicleType.BIKE) {return true;}
        if(walkable && type == VehicleType.WALK) {return true;}
        else {return false;} //if none of the above allowed combinations

    }

}

