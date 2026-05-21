package PathFinder;

public class DirectedEdge {
    private final int from;
    private final int to;
    private final float length;
    private final float speedLimit;
    private final boolean driveable;
    private final boolean bikeable;
    private final boolean walkable;

    /**constructer to initialise all fields*/
    public DirectedEdge(int from, int to, float length, float speedLimit, boolean driveable, boolean bikeable, boolean walkable) {
        this.from = from;
        this.to = to;
        this.length = length;
        this.speedLimit = speedLimit;
        this.driveable = driveable;
        this.bikeable = bikeable;
        this.walkable = walkable;
    }

    /** Calculates the weight of the edge based on speed for the vehicle type and the actual distance*/
    public float weight(VehicleType type){
        //if the vehicle type is not allowed to traverse an edge set the distance to positive infinity
        // any other allowed edge will thus be shorter and this edge is not in the final shortest path
        if(!isAllowed(type)){return Float.POSITIVE_INFINITY;}

        if(type == VehicleType.CAR){return length / speedLimit;}//gives the time it takes to traverse for the car
        else if (type == VehicleType.WALK){
            float speed = 4.5F; //Based on: https://www.medicalnewstoday.com/articles/average-walking-speed#average-speed-by-age
            return length /speed;}
        else{float speed = 20; //Based on: https://www.bicyclesandcycling.com/average-cycling-speed-for-beginner-and-experienced-cyclists/
            return length / speed;} //gives the distance for bike
    }

    /**Private method for code readability to decide if the path is allowed for the chosen vehicle type*/
    private boolean isAllowed(VehicleType type){
        if(driveable && type == VehicleType.CAR) {return true;}
        if(bikeable && type == VehicleType.BIKE) {return true;}
        if(walkable && type == VehicleType.WALK) {return true;}
        else {return false;} //if none of the above allowed combinations

    }

    /**Used in Dijkstra & EdgeWeightedDigraph to access private field from*/
    public int from() {
        return from;
    }

    /**Used in Dijkstra & EdgeWeightedDigraph to access private field to*/
    public int to() {
        return to;
    }

    /**Used in EdgeWeightedDigraph to access private field length*/
    public float getLength() {
        return length;
    }
}

