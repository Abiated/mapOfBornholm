package TagHelper;

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
        // natural { cliff, water, wood, beach, grassland, coastline}

    }
    public static boolean isRailway(OsmWay osmway){
        return osmway.getTags().containsKey("railway");

    }
    public static boolean isLandUse(OsmWay osmway){
        return osmway.getTags().containsKey("landuse");
        // landUse{ Forrest, Residential, farmland, industrial}
    }
    public static boolean isWaterWay(OsmWay osmway){
        return  osmway.getTags().containsKey("waterway");
        // River Stream, Canal
    }
    public static boolean isAeroway(OsmWay osmway){
        return  osmway.getTags().containsKey("aeroway");
    }
    public static boolean isAmenity(OsmWay  osmway){
        return osmway.getTags().containsKey("amenity");
    }
    public static boolean isLeisure(OsmWay osmway){
        return osmway.getTags().containsKey("leisure");
    }
    public static boolean isPlace(OsmWay osmway){
        return osmway.getTags().containsKey("place");
    }
    public static boolean isStation(OsmWay way) {
        return "station".equals(way.getTags().get("railway")); // fixed!
    }

    public static NaturalType getNaturalType (OsmWay osmway){
        String value=osmway.getTags().get("natural");
        if(value==null){
            return NaturalType.UNKNOWN;
        }
        return switch(value){
            case "bay" -> NaturalType.BAY;
            case "coastline" -> NaturalType.COASTLINE;
            case "shoal" -> NaturalType.SHOAL;
            case "shrub" -> NaturalType.SHRUB;

            case "water" -> NaturalType.WATER;
            case "grassland" -> NaturalType.GRASSLAND;
            case "scrub" -> NaturalType.SCRUB;
            case "tree" -> NaturalType.TREE;
            case "tree_row" -> NaturalType.TREE_ROW;
            case "wetland" -> NaturalType.WETLAND;
            case "wood" ->  NaturalType.WOOD;

            case "beach" -> NaturalType.BEACH;
            case "cape" -> NaturalType.CAPE;
            case "cliff" -> NaturalType.CLIFF;
            case "peak" -> NaturalType.PEAK;
            case "scree" -> NaturalType.SCREE;
            case "shingle" -> NaturalType.SHINGLE;

            default -> NaturalType.UNKNOWN;

        };
    }

    public static HighwayType getHighwayType(OsmWay osmway){
        String value=osmway.getTags().get("highway");
        if(value==null){
            return HighwayType.UNKNOWN;
        }
        return switch(value){
            case "motorway" -> HighwayType.MOTORWAY; //findes nok hverken på tunoe eller bornholm
            case "pedestrian" -> HighwayType.PEDESTRIAN;
            case "residential" -> HighwayType.RESIDENTIAL;
            case "path" ->  HighwayType.PATH;
            case "footway" -> HighwayType.FOOTWAY;
            case "cycleway" -> HighwayType.CYCLEWAY; //findes ikke på tunoe
            case "primary" -> HighwayType.PRIMARY; //findes ikke på tunoe

            default -> HighwayType.UNKNOWN;
        };



    /*TIL RENDERING:
    HighwayType type = TagToLife.getHighwayType(way);

        if (type.driveable) { skal tegnes som vej }
        if (type.walkable)  { // skal tegnes som sti }
        if (type.cyclable)  { // være kreativ }
     */
    }

    public static BuildingType getBuildingType(OsmWay osmway){
        String value=osmway.getTags().get("building");
        if(value==null){
            return BuildingType.UNKNOWN;
        }
        return switch(value){
            case "yes" -> BuildingType.YES;
            case "house" -> BuildingType.HOUSE;
            case "detached" -> BuildingType.DETACHED;
            case "farm" -> BuildingType.FARM;
            case "church" -> BuildingType.CHURCH;
            case "retail" -> BuildingType.RETAIL;
            case "greenhouse" -> BuildingType.GREENHOUSE;
            case "hut"  -> BuildingType.HUT;
            case "shed" -> BuildingType.SHED;
            case "roof" -> BuildingType.ROOF;
            case "construction" -> BuildingType.CONSTRUCTION;

            default -> BuildingType.UNKNOWN;
        };
    }
    public static LandUseType getLandUseType(OsmWay osmway){
        String value=osmway.getTags().get("landuse");
        if(value==null){
            return LandUseType.UNKNOWN;
        }
        return switch (value){
            case "residential" -> LandUseType.RESIDENTIAL;
            case "farmland" -> LandUseType.FARMLAND;
            case "farmyard" -> LandUseType.FARMYARD; //ved ikke hvad forskellen mellem disse to er
            case "forest" -> LandUseType.FOREST;
            case "meadow" -> LandUseType.MEADOW;
            case "orchard" -> LandUseType.ORCHARD;
            case "cemetery" -> LandUseType.CEMETERY;
            case "flowerbed" -> LandUseType.FLOWERBED;
            case "Vineyard" -> LandUseType.VINEYARD; //bruges ikke (findes på bornholmd?)
            case "greenhouse_horticulture" -> LandUseType.GREENHOUSE_HORTICULTURE;//bruges ikke
            case "military" -> LandUseType.MILITARY;//bruges ikke
            case "aquaculture" -> LandUseType.AQUACULTURE;//bruges ikke
            case "landfill" -> LandUseType.LANDFILL;//bruges ikke
            case "salt_pond" -> LandUseType.LANDFILL; //bruges ikke
            case "commercial" -> LandUseType.COMMERCIAL; //bruges ikke
            case "industrial" -> LandUseType.INDUSTRIAL; //bruges ikke
            case "retail" -> LandUseType.RETAIL; //bruges ikke
            case "religious" -> LandUseType.RELIGIOUS; //ikke brugt
            case "institutional" -> LandUseType.INSTITUTIONAL; //bruges ikke

            default -> LandUseType.UNKNOWN;


        };

    }
}

