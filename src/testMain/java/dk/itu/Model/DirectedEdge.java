package dk.itu.Model;

public class DirectedEdge {
    //TODO: deicide if we use this class or directed weighted edge data type p. 642
    int from;
    int to;
    float weight; //float for memory - see if we get issues when trying to calculate with it.
    float length; //set as a float to preserve memory. Might have to be a double for precision.
    float speedLimit;

    boolean oneWay; //there is a tag for OsmWay for this.
    boolean driveable;
    boolean bikeable; //should walkable and cyclable be in one boolean ro do we expect there to be cases when they differ?
    boolean walkable;

    //constructor should take what fields?? All of them?
    DirectedEdge(){
        //TODO: what fields should we initialize? What do we get from the parser. What is missing?

    }

    float weight(DirectedEdge edge, VehicleType type){
        //if the vehicle type is not allowed to traverse an edge set the distance to positive infinity
        // any other allowed edge will thus be shorter and this edge is not in the final shortest path
        if(!isAllowed(edge,type)){return Float.POSITIVE_INFINITY;}

        //TODO: decide if we access the fields directly or make get methods for length and speedLimit
        if(type == VehicleType.CAR){return edge.length/edge.speedLimit;} //gives the time it takes to traverse for the car
        else{return edge.length;} //gives the distance for bike and walk
    }


    //this has been rewritten with enum. Previously we had abstract class vehicle with subclasses Car, Bike, Walk
    //seems to be more minimalistic and saves us a bunch of classes.
    //can be written back with type instance of Car if we need more information stored and have use of polymorphism
    boolean isAllowed(DirectedEdge edge, VehicleType type){
        if(edge.driveable && type == VehicleType.CAR) {return true;}
        if(edge.bikeable && type == VehicleType.BIKE) {return true;}
        if(edge.walkable && type == VehicleType.WALK) {return true;}
        else {return false;} //if none of the above allowed combinations

    }


    // helper methods
    public float getLength() {
        return length;
    }

    public float getSpeedLimit() {
        return speedLimit;
    }
}




/* Vi holder referencen til objektet når DE og EWG refererer til hinanden
Ny instant => 8 bytes

ADJ list
*
*
* */
