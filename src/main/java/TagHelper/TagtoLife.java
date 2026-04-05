package TagHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.itu.parser.models.OsmWay;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TagtoLife {
    private static final JsonNode config = loadConfig();

    private static JsonNode loadConfig() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = TagtoLife.class.getClassLoader().getResourceAsStream("/tags.json");
            if (is == null) {
                System.out.println("tags.json not found");
            }
            return mapper.readTree(is);
        } catch (IOException e) {
            System.out.println("Error loading config file");
            return null;
        }
    }

    public static boolean isDriveable(OsmWay way) {
        String value = way.getTags().get("highway");
        if (value == null || config == null) return false;

        return config.path("transportation")
                .path("highway")
                .path(value)
                .path("driveable")
                .asBoolean(false);
        }

    public static boolean isWalkable(OsmWay way) {
        String value = way.getTags().get("highway");
        if (value == null || config == null) return false;
        return config.path("highway")
                .path(value)
                .path("walkable")
                .asBoolean(false);
    }


    public static boolean isManMade(OsmWay way) {
        return way.getTags().containsKey("man_made");
    }

    public static boolean isBarrier(OsmWay way) {
        return way.getTags().containsKey("barrier");

    }

    public static boolean isBuilding(OsmWay osmway) {
        return osmway.getTags().containsKey("building");
        //building {yes, house, apartement, commercial, school
    }

    public static boolean isHighway(OsmWay osmway) {
        return osmway.getTags().containsKey("highway");
    }

    public static boolean isNatural(OsmWay osmway) {
        return osmway.getTags().containsKey("natural");
        // natural { cliff, water, wood, beach, grassland, coastline}

    }


    public static boolean isLandUse(OsmWay osmway) {
        return osmway.getTags().containsKey("landuse");
        // landUse{ Forrest, Residential, farmland, industrial}
    }

    public static boolean isAmenity(OsmWay osmway) {
        return osmway.getTags().containsKey("amenity");
    }


    public static TransportationType getTransportationType(OsmWay osmway) {
        String value = osmway.getTags().get("highway");
        if (value == null) {
            return TransportationType.UNKNOWN;
        }
        return switch (value) {
            case "motorway" -> TransportationType.MOTORWAY; //findes nok hverken på tunoe eller bornholm
            case "pedestrian" -> TransportationType.PEDESTRIAN;
            case "residential" -> TransportationType.RESIDENTIAL;
            case "path" -> TransportationType.PATH;
            case "footway" -> TransportationType.FOOTWAY;
            case "cycleway" -> TransportationType.CYCLEWAY; //findes ikke på tunoe
            case "primary" -> TransportationType.PRIMARY; //findes ikke på tunoe

            default -> TransportationType.UNKNOWN;
        };

    }


    public static StructureType getStructureType(OsmWay way) {
        String value = way.getTags().get("building");
        if (value == null) {
            value = way.getTags().get("man_made");
        }
        if (value == null) {
            value = way.getTags().get("barrier");
        }
        if (value == null) {
            return StructureType.UNKNOWN;
        }
        return switch (value) {
            case "yes" -> StructureType.YES;
            case "house" -> StructureType.HOUSE;
            case "detached" -> StructureType.DETACHED;
            case "farm" -> StructureType.FARM;
            case "church" -> StructureType.CHURCH;
            case "retail" -> StructureType.RETAIL;
            case "greenhouse" -> StructureType.GREENHOUSE;
            case "hut" -> StructureType.HUT;
            case "shed" -> StructureType.SHED;
            case "roof" -> StructureType.ROOF;
            case "construction" -> StructureType.CONSTRUCTION;

            default -> StructureType.UNKNOWN;
        };
    }

    public static LandscapeType getLandscapeType(OsmWay way) {
        String value = way.getTags().get("landuse");
        if (value == null) {
            value = way.getTags().get("natural");}
        if (value == null) {
            return LandscapeType.UNKNOWN;
        }
        return switch (value) {
            case "residential" -> LandscapeType.RESIDENTIAL;
            case "farmland" -> LandscapeType.FARMLAND;
            case "farmyard" -> LandscapeType.FARMYARD; //ved ikke hvad forskellen mellem disse to er
            case "forest" -> LandscapeType.FOREST;
            case "meadow" -> LandscapeType.MEADOW;
            case "orchard" -> LandscapeType.ORCHARD;
            case "cemetery" -> LandscapeType.CEMETERY;
            case "flowerbed" -> LandscapeType.FLOWERBED;
            case "Vineyard" -> LandscapeType.VINEYARD; //bruges ikke (findes på bornholmd?)
            case "greenhouse_horticulture" -> LandscapeType.GREENHOUSE_HORTICULTURE;//bruges ikke
            case "military" -> LandscapeType.MILITARY;//bruges ikke
            case "aquaculture" -> LandscapeType.AQUACULTURE;//bruges ikke
            case "landfill" -> LandscapeType.LANDFILL;//bruges ikke
            case "salt_pond" -> LandscapeType.LANDFILL; //bruges ikke
            case "commercial" -> LandscapeType.COMMERCIAL; //bruges ikke
            case "industrial" -> LandscapeType.INDUSTRIAL; //bruges ikke
            case "retail" -> LandscapeType.RETAIL; //bruges ikke
            case "religious" -> LandscapeType.RELIGIOUS; //ikke brugt
            case "institutional" -> LandscapeType.INSTITUTIONAL; //bruges ikke
            //Natural
            case "bay" -> LandscapeType.BAY;
            case "coastline" -> LandscapeType.COASTLINE;
            case "shoal" -> LandscapeType.SHOAL;
            case "shrub" -> LandscapeType.SHRUB;
            case "water" -> LandscapeType.WATER;
            case "grassland" -> LandscapeType.GRASSLAND;
            case "scrub" -> LandscapeType.SCRUB;
            case "tree" -> LandscapeType.TREE;
            case "tree_row" -> LandscapeType.TREE_ROW;
            case "wetland" -> LandscapeType.WETLAND;
            case "wood" -> LandscapeType.WOOD;
            case "beach" -> LandscapeType.BEACH;
            case "cape" -> LandscapeType.CAPE;
            case "cliff" -> LandscapeType.CLIFF;
            case "peak" -> LandscapeType.PEAK;
            case "scree" -> LandscapeType.SCREE;
            case "shingle" -> LandscapeType.SHINGLE;

            default -> LandscapeType.UNKNOWN;


        };

    }

    public static PointsOfInterestType getPointsOfInterest (OsmWay way) {
        String value = way.getTags().get("tourism");
        if (value == null) {
            value = way.getTags().get("tourism");
        }
            if (value == null) {
                return PointsOfInterestType.UNKNOWN;
            }
            return switch (value) {
                case "camp_site" -> PointsOfInterestType.CAMP_SITE;
                case "hotel" -> PointsOfInterestType.HOTEL;
                case "apartment" -> PointsOfInterestType.APARTMENT;
                case "chalet" -> PointsOfInterestType.CHALET;
                case "museum" -> PointsOfInterestType.MUSEUM;
                case "viewpoint" -> PointsOfInterestType.VIEWPOINT;
                case "picnic_site" -> PointsOfInterestType.PICNIC_SITE;
                case "information" -> PointsOfInterestType.INFORMATION;
                default -> PointsOfInterestType.UNKNOWN;
            };
        }

    public static Direction getDirection( OsmWay way){
        String value = way.getTags().get("oneway");

        if(value == null) {
            value=way.getTags().get("both");
        }
        if(value == null) {
            return Direction.UNKNOWN;
        }
        return switch (value){
            case "yes" -> Direction.ONEWAY;
            case "no" -> Direction.BOTH;
            default -> Direction.UNKNOWN;
        };
    }
    //til brug i dir:
   /* Direction dir = TagToLife.getDirection(way);

switch (dir) {
        case BOTH    -> { addEdge(A, B); addEdge(B, A); }
        case ONEWAY  -> { addEdge(A, B); }
        case UNKNOWN -> { addEdge(A, B); addEdge(B, A); } // safe fallback
    }
*/

    public static Address getAddress(OsmWay way) {
        HashMap<String, String> tags = way.getTags();
        if (!tags.containsKey("addr:street")) return null;
        return new Address(
                tags.getOrDefault("addr:street", ""),
                tags.getOrDefault("addr:housenumber", ""),
                tags.getOrDefault("addr:city", ""),
                tags.getOrDefault("addr:postcode", ""),
                tags.getOrDefault("addr:country", "")
        );
    }

    public static int getWidth(OsmWay way){
        if (config==null) return 1;
        Map<String, String> keyToCategory= Map.of(
                "highway", "transportation"
        );
        for (Map.Entry<String, String> entry : keyToCategory.entrySet()) {
            String tagKey = entry.getKey();
            String category = entry.getValue();
            String value = way.getTags().get(tagKey);
            if(value!=null){
                JsonNode widthNode = config
                        .path(category)
                        .path(tagKey)
                        .path(value)
                        .path("width");

                if (!widthNode.isMissingNode()) {
                    return widthNode.asInt(1);
                }
            }
        }
        return 1;

    }

    public static String getColor(OsmWay way) {
        if (config == null) return "#ffff";
        Map<String, String> keyToCategory = Map.of(
                "highway", "transportation",
                "natural", "landscape",
                "landuse", "landscape",
                "building", "structures",
                "man_made", "structures",
                "barrier", "structures",
                "amenity", "pointsOfInterest",
                "tourism", "pointsOfInterest"

        );

        for (Map.Entry<String, String> entry : keyToCategory.entrySet()) {
            String tagKey = entry.getKey();
            String category = entry.getValue();
            String value = way.getTags().get(tagKey);
            if (value != null) {
                JsonNode colorNode = config
                        .path(category)
                        .path(tagKey)
                        .path(value)
                        .path("color");

                if (!colorNode.isMissingNode()) {
                    return colorNode.asText();
                }

            }


        }

        return "#cccccc#";

    }

    public static boolean shouldFill(OsmWay way) {
        if (config == null) return false;
        Map<String, String> keyToCategory = Map.of(
                "landuse", "landscape",
                "natural", "landscape",
                "building", "structures",
                "man_made", "structures",
                "barrier", "structures",
                "tourism", "pointsOfInterest",
                "amenity", "pointsOfInterest"
        );


        for (Map.Entry<String, String> entry : keyToCategory.entrySet()) {
            String tagKey = entry.getKey();
            String category = entry.getValue();
            String value = way.getTags().get(tagKey);
            if (value != null) {
                JsonNode fillNode = config
                        .path(category)
                        .path(tagKey)
                        .path(value)
                        .path("fill");

                if (!fillNode.isMissingNode()) {
                    return fillNode.asBoolean(false);
                }

            }


        }

        return false;

    }
}

