package dk.itu.parser;

import dk.itu.parser.models.OsmWay;

public class TagtoLife {
    public static boolean isBuilding(OsmWay osmway){
            return osmway.getTags().containsKey("building");
        //building {yes, house, apartement, commercial, school
    }
    public static boolean isHighway(OsmWay osmway){
        return osmway.getTags().containsKey("highway");
    }
    public static boolean isNatural(OsmWay osmway){
        return osmway.getTags().containsKey("natural");
        // natural { water, wood, beach, grassland, coastline}

    }
    public static boolean isHouse(OsmWay osmway){
        return osmway.getTags().containsKey("house");

    }
    public static boolean isStation(OsmWay osmway){
        return osmway.getTags().containsKey("station");

    }
    public static boolean isLandUse(OsmWay osmway){
        return osmway.getTags().containsKey("land");
        // landUse{ Forrest, Residential, farmland, industrial}
    }
    public static boolean isWaterWay(OsmWay osmway){
        return  osmway.getTags().containsKey("waterway");
        // River Stream, Canal
    }
    public static boolean isAeroway(OsmWay osmway){
        return  osmway.getTags().containsKey("aeroway");
    }






    public static HighwayType getHighwayType(OsmWay osmway){
        String value=osmway.getTags().get("highway");
        if(value==null){
            return HighwayType.Unknown;
        }
        return switch(value){
            case "motorway" -> HighwayType.Motorway;
            case "pedestrian" -> HighwayType.Pedestrian;
            case "residential" -> HighwayType.Residential;
            case "Path" ->  HighwayType.Path;
            case "footway" -> HighwayType.Footway;
            case "cycleway" -> HighwayType.Cycleway;
            case "service" -> HighwayType.Service;

            default -> null;
        };
    }
}

