package TagHelper;

public enum TransportationType {
    //driveable
    MOTORWAY(false, false,false),
    PRIMARY(false, false,false),
    SECONDARY(false, false,false),
    RESIDENTIAL(false, false,false),
    SERVICE(false, false,false),
    //Walkable
    PEDESTRIAN(false, false, false),
    FOOTWAY(false, false, false),
    PATH(false,true, false),

    CYCLEWAY(false, true, true),
    UNKNOWN(false, false, false);


    public final boolean driveable;
    public final boolean walkable;
    public final boolean bikeable;

    TransportationType(boolean driveable, boolean walkable, boolean bikeable) {
        this.driveable = driveable;
        this.walkable = walkable;
        this.bikeable = bikeable;
    }

}
