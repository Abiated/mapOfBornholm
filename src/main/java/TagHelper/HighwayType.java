package TagHelper;

public enum HighwayType{
    MOTORWAY(false, false),
    PRIMARY(false, false),
    RESIDENTIAL(false, false),
    PEDESTRIAN(false, false),
    FOOTWAY(false, false),
    //CYCLEWAY(false, false),
    PATH(false,true),
    UNKNOWN(false, false);

    public final boolean driveable;
    public final boolean walkable;
    //public final boolean bikeable;

    HighwayType(boolean driveable, boolean walkable) {
        this.driveable = driveable;
        this.walkable = walkable;
        //this.cycleable = bikeable;
    }
}
