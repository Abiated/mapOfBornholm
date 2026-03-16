package dk.itu.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dk.itu.parser.interfaces.IParser;
import dk.itu.renderer.osm.*;

public class Parser implements IParser {

    private final String fileName;
    private final List<Float> boundingBox = new ArrayList<>();
    private final HashMap<Long, OsmNode> osmNodeMap = new HashMap<>();
    private final HashMap<Long, OsmWay> osmWayMap = new HashMap<>();
    private final HashMap<Long, OsmRelation> osmRelationMap = new HashMap<>();

    public Parser(String filename) {
        this.fileName = filename;
    }

    @Override
    public void parse() {
        try {
            InputStream is = Parser.class.getResourceAsStream("/data/" + fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            br.readLine();
            br.readLine();
            String bounds = br.readLine();
            float minlat = (float)getAttributeDouble(bounds,"minlat");
            float minlon = (float)getAttributeDouble(bounds,"minlon");
            float maxlat = (float)getAttributeDouble(bounds,"maxlat");
            float maxlon = (float)getAttributeDouble(bounds,"maxlon");
            boundingBox.add(minlat);
            boundingBox.add(minlon);
            boundingBox.add(maxlat);
            boundingBox.add(maxlon);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(" <node")) {
                    long id = getAttributeLong(line,"id");
                    double lat = getAttributeDouble(line,"lat");
                    double lon = getAttributeDouble(line,"lon");
                    osmNodeMap.put(id, new OsmNode(id, (float)lat, (float)lon));
                }
                if (line.startsWith(" <way")) {
                    long WayId = getAttributeLong(line,"id");
                    List<OsmNode> nodes = new ArrayList<>();
                    HashMap<String,String> tags = new HashMap<>();
                    while ((line = br.readLine()).startsWith("  <nd")) {
                        long NodeRef = getAttributeLong(line,"ref");
                        nodes.add(osmNodeMap.get(NodeRef));
                    }
                    String key1 = getAttribute(line,"k");
                    String value1 = getAttribute(line,"v");
                    tags.put(key1,value1);
                    while ((line = br.readLine()).startsWith("  <tag")) {
                        String key = getAttribute(line,"k");
                        String value = getAttribute(line,"v");
                        tags.put(key,value);
                    }
                    osmWayMap.put(WayId,new OsmWay(WayId,nodes,tags));
                }
                if (line.startsWith(" <relation")) {
                    long RelationId = getAttributeLong(line, "id");
                    List<Member> members = new ArrayList<>();
                    HashMap<String, String> tags = new HashMap<>();
                    while ((line = br.readLine()).startsWith("  <member")) {
                        String type = getAttribute(line, "type");
                        String role = getAttribute(line, "role");
                        long ref = getAttributeLong(line, "ref");
                        OsmElement element;
                        switch (type) {
                            case "node" -> element = osmNodeMap.get(ref);
                            case "way" -> element = osmWayMap.get(ref);
                            case "relation" -> element = osmRelationMap.get(ref);
                            default -> element = null;
                        }
                        if (element == null && type.equals("relation")) {
                            element = new OsmRelation(ref,new ArrayList<>(), new HashMap<>());
                            osmRelationMap.put(ref,(OsmRelation)element);
                        }
                        if (element != null) {
                            Member member = new Member(element, role);
                            members.add(member);
                        }
                    }
                    String key1 = getAttribute(line,"k");
                    String value1 = getAttribute(line,"v");
                    tags.put(key1,value1);
                    while ((line = br.readLine()).startsWith("  <tag")) {
                        String key = getAttribute(line,"k");
                        String value = getAttribute(line,"v");
                        tags.put(key,value);
                    }
                    OsmRelation relation = new OsmRelation(RelationId,members,tags);
                    osmRelationMap.put(RelationId,relation);
                }
                else {

                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getAttribute(String s, String key) {
        String pattern = key + "=\"";
        int start = s.indexOf(pattern);
        if (start == -1) {
            return null;
        }
        int valueStart = start + pattern.length();
        int valueEnd = s.indexOf('"', valueStart);
        return s.substring(valueStart, valueEnd);
    }

    public double getAttributeDouble(String s, String key) {
        String val = getAttribute(s, key);
        if (val == null) {
            return Double.NaN;
        }
        return Double.parseDouble(val);
    }

    public long getAttributeLong(String s, String key) {
        String val = getAttribute(s, key);
        if (val == null) {
            return 0L;
        }
        return Long.parseLong(val);
    }

    //DO NOT MODIFY BELOW GETTER METHODS
    @Override
    public List<Float> getBoundingBox() {
        return boundingBox;
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
}
