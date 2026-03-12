package dk.itu.parser.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import dk.itu.parser.interfaces.IParser;
import dk.itu.parser.models.Member;
import dk.itu.parser.models.OsmElement;
import dk.itu.parser.models.OsmNode;
import dk.itu.parser.models.OsmRelation;
import dk.itu.parser.models.OsmWay;

/// This 'parser' is solely used for testing and is not relevant for the assignment
public class TestParser implements IParser {

    String fileName;
    private final List<Float> boundingBox = new ArrayList<>();
    private final HashMap<Long, OsmNode> osmNodeMap = new HashMap<>();
    private final HashMap<Long, OsmWay> osmWayMap = new HashMap<>();
    private final HashMap<Long, OsmRelation> osmRelationMap = new HashMap<>();

    ObjectMapper mapper = new ObjectMapper();

    public TestParser(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void parse() {
        try {
            InputStream is = TestParser.class.getClassLoader().getResourceAsStream("jsonData/" + fileName);
            if (is == null) {
                System.out.println("File not found");
                return;
            }

            JsonNode root = mapper.readTree(is);

            parseBoundingBox(root);
            parseNodes(root);
            parseWays(root);
            parseRelations(root);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        //TODO: Load JSON content based on fileName
    }

    private void parseBoundingBox(JsonNode root) {
        JsonNode bounds = root.path("osm").path("bounds");

        Float minLat = BigDecimal.valueOf(bounds.path("@minlat").asDouble()).floatValue();
        Float minLon = BigDecimal.valueOf(bounds.path("@minlon").asDouble()).floatValue();
        Float maxLat = BigDecimal.valueOf(bounds.path("@maxlat").asDouble()).floatValue();
        Float maxLon = BigDecimal.valueOf(bounds.path("@maxlon").asDouble()).floatValue();

        boundingBox.addAll(List.of(minLat, minLon, maxLat, maxLon));
    }

    private void parseNodes(JsonNode root) {
        JsonNode nodes = root.path("osm").path("node");

        for (JsonNode n : nodes) {
            long id = n.path("@id").asLong();
            float lat = BigDecimal.valueOf(n.path("@lat").asDouble()).floatValue();
            float lon = BigDecimal.valueOf(n.path("@lon").asDouble()).floatValue();

            osmNodeMap.put(id, new OsmNode(id, lat, lon));
        }
    }

    private void parseWays(JsonNode root) {
        JsonNode ways = root.path("osm").path("way");

        for (JsonNode w : ways) {
            long id = w.path("@id").asLong();

            //Construct node references
            List<OsmNode> nodesInWay = new ArrayList<>();
            JsonNode ndNode = w.path("nd");
            if (!ndNode.isArray()) {
                return; //TODO: Decide what to do?
            }
            for (JsonNode nd : ndNode) {
                long nodeRef = nd.path("@ref").asLong();
                nodesInWay.add(osmNodeMap.get(nodeRef));
            }

            //Construct tags
            HashMap<String, String> tagsInWay = new HashMap<>();
            JsonNode tagNode = w.path("tag");

            ArrayNode tagArray = mapper.createArrayNode();
            if (tagNode.isArray()) {
                tagArray = (ArrayNode) tagNode;
            } else if (!tagNode.isMissingNode() && !tagNode.isNull()) {
                tagArray.add(tagNode);
            }

            for (JsonNode tag : tagArray) {
                String key = tag.path("@k").asText();
                String value = tag.path("@v").asText();
                tagsInWay.put(key, value);
            }

            osmWayMap.put(id, new OsmWay(id, nodesInWay, tagsInWay));
        }
    }

    private void parseRelations(JsonNode root) {
        JsonNode relations = root.path("osm").path("relation");

        for (JsonNode r : relations) {
            long id = r.path("@id").asLong();

            //Construct member references
            List<Member> members = new ArrayList<>();
            JsonNode memberNode = r.path("member");
            if (!memberNode.isArray()) {
                return; //TODO: Decide what do to?
            }
            for (JsonNode m : memberNode) {
                String type = m.path("@type").asText();
                Long mId = m.path("@ref").asLong();
                String role = m.path("@role").asText();

                OsmElement mElement = null;
                switch (type) {
                    case "node" -> {
                        if (!osmNodeMap.containsKey(mId)) {
                            continue;
                        }
                        mElement = osmNodeMap.get(mId);
                    }
                    case "way" -> {
                        if (!osmWayMap.containsKey(mId)) {
                            continue;
                        }
                        mElement = osmWayMap.get(mId);
                    }
                    case "relation" -> {
                        boolean relationExists = osmRelationMap.containsKey(mId);
                        if (relationExists) {
                            mElement = osmRelationMap.get(mId);
                        } else {
                            OsmRelation relation = new OsmRelation(mId, List.of(), new HashMap<>());
                            osmRelationMap.put(mId, relation);
                            mElement = relation;
                        }
                    }
                }

                members.add(new Member(mElement, role));
            }

            //Construct tags
            HashMap<String, String> tagsInRelation = new HashMap<>();
            JsonNode tagNode = r.path("tag");
            if (!tagNode.isArray()) {
                return; //TODO: Decide what to do?
            }
            for (JsonNode tag : tagNode) {
                String key = tag.path("@k").asText();
                String value = tag.path("@v").asText();
                tagsInRelation.put(key, value);
            }

            //Determine if relation already exists
            boolean relationExists = osmRelationMap.containsKey(id);
            if (relationExists) {
                OsmRelation relation = osmRelationMap.get(id);
                relation.setMembers(members);
                relation.setTags(tagsInRelation);
            } else {
                osmRelationMap.put(id, new OsmRelation(id, members, tagsInRelation));
            }
        }
    }

    @Override
    public HashMap<Long, OsmNode> getOsmNodeMap() {
        return osmNodeMap;
    }

    @Override
    public HashMap<Long, OsmWay> getOsmWayMap() {
        return osmWayMap;
    }

    @Override
    public HashMap<Long, OsmRelation> getOsmRelationMap() {
        return osmRelationMap;
    }

    @Override
    public List<Float> getBoundingBox() {
        return boundingBox;
    }
}
