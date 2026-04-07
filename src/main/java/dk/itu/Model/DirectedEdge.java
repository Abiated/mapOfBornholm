package dk.itu.Model;

public class DirectedEdge {
    //TODO: deicide if we use this class or directed weighted edge data type p. 642
    int from;
    int to;
    float weight;
    float length;
    float speedLimit;

    boolean driveable;
    boolean bikeable; //should walkable and cyclable be in one boolean or do we expect there to be cases when they differ?
    boolean walkable;

    DirectedEdge(){//TODO: what fields should we initialize if any?
    }

    float weight(DirectedEdge edge, VehicleType type){
        //if the vehicle type is not allowed to traverse an edge set the distance to positive infinity
        // any other allowed edge will thus be shorter and this edge is not in the final shortest path
        if(!isAllowed(edge,type)){return Float.POSITIVE_INFINITY;}

        //TODO: decide if we access the fields directly or make getter methods for length and speedLimit
        if(type == VehicleType.CAR){return edge.length/edge.speedLimit;} //gives the time it takes to traverse for the car
        else{return edge.length;} //gives the distance for bike and walk
    }

    boolean isAllowed(DirectedEdge edge, VehicleType type){
        if(edge.driveable && type == VehicleType.CAR) {return true;}
        if(edge.bikeable && type == VehicleType.BIKE) {return true;}
        if(edge.walkable && type == VehicleType.WALK) {return true;}
        else {return false;} //if none of the above allowed combinations

    }

}


