package TagHelper;

public enum HighwayType{
    MOTORWAY(true, false, false),
    PRIMARY(true,false,false),
    RESIDENTIAL(true, true, false),
    PEDESTRIAN(false, true, false),
    FOOTWAY(false, true, false),
    CYCLEWAY(false, false, true),
    PATH(false,true,true),
    UNKNOWN(false, false, false);

    public final boolean driveable;
    public final boolean walkable;
    public final boolean bikeable;

    HighwayType(boolean driveable, boolean walkable, boolean bikeable) {
        this.driveable = driveable;
        this.walkable = walkable;
        this.bikeable = bikeable;
    }
}
