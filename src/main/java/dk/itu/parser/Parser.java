package dk.itu.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.itu.parser.models.*;
import dk.itu.parser.interfaces.*;
public class Parser implements IParser {

    private final String fileName;

    private final List<Float> boundingBox = new ArrayList<>();
    private final HashMap<Long, OsmNode> osmNodeMap = new HashMap<>();
    private final HashMap<Long, OsmWay> osmWayMap = new HashMap<>();
    private final HashMap<Long, OsmRelation> osmRelationMap = new HashMap<>();
    private final Map<Long, RelationTemp> relationTempMap = new HashMap<>();

    public Parser(String filename) {

        this.fileName = filename;
    }

    @Override
    public void parse() {
        try {
            InputStream is = Parser.class.getResourceAsStream("/data/" + fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));


            boolean inWay = false;
            boolean inRelation = false;

            long currentWayId = 0;
            long currentRelationId = 0;

            List<OsmNode> nodes1 = new ArrayList<>();
            HashMap<String, String> wayTags = new HashMap<>();


            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("<bounds")) {
                    float minlat = getAttributeFloat(line, "minlat");
                    float minlon = getAttributeFloat(line, "minlon");
                    float maxlat = getAttributeFloat(line, "maxlat");
                    float maxlon = getAttributeFloat(line, "maxlon");
                    boundingBox.add(minlat);
                    boundingBox.add(minlon);
                    boundingBox.add(maxlat);
                    boundingBox.add(maxlon);
                }

                if (line.startsWith("<node")) {

                    long id = getAttributeLong(line, "id");
                    float lat = getAttributeFloat(line, "lat");
                    float lon = getAttributeFloat(line, "lon");

                    OsmNode osm = new OsmNode(id, lat, lon);
                    osmNodeMap.put(id, osm);

                    continue;
                }

                if (line.startsWith("<way")) {
                    inWay = true;
                    currentWayId = getAttributeLong(line, "id");
                    nodes1 = new ArrayList<>();
                    wayTags = new HashMap<>();

                    continue;
                }

                if (line.startsWith("</way>")) {
                    OsmWay way = new OsmWay(currentWayId, nodes1, wayTags);
                    osmWayMap.put(currentWayId, way);

                    inWay = false;

                    continue;
                }


                if (inWay) {

                    if (line.startsWith("<nd")) {
                        long ref = getAttributeLong(line, "ref");
                        OsmNode node = osmNodeMap.get(ref);

                        if (node != null) {
                            nodes1.add(node);
                        }

                        continue;
                    }

                    if (line.startsWith("<tag")) {
                        String k = getAttribute(line, "k");
                        String v = getAttribute(line, "v");

                        if (k != null && v != null) {
                            wayTags.put(k, v);


                        }

                        continue;
                    }
                }

                if (line.startsWith("<relation")) {
                    long id = getAttributeLong(line, "id");

                    RelationTemp tmp = new RelationTemp(id);
                    relationTempMap.put(id, tmp);
                    osmRelationMap.put(id, new OsmRelation(id, new ArrayList<>(), tmp.tags));

                    inRelation = true;
                    currentRelationId = id;
                    continue;

                }

                if (line.startsWith("</relation>")) {
                    inRelation = false;
                    currentRelationId = 0;
                    continue;
                }

                if (inRelation) {
                    RelationTemp tmp = relationTempMap.get(currentRelationId);


                    if (line.startsWith("<member")) {
                        String type = getAttribute(line, "type");
                        long ref = getAttributeLong(line, "ref");
                        String role = getAttribute(line, "role");

                        if (type != null) {
                            tmp.memberRefs.add(new MemberRef(type, ref, role));

                            if ("relation".equals(type) && !osmRelationMap.containsKey(ref)) {
                                osmRelationMap.put(ref, new OsmRelation(ref, new ArrayList<>(), new HashMap<>()));
                            }
                        }
                        continue;

                    }

                    if (line.startsWith("<tag")) {
                        String k = getAttribute(line, "k");
                        String v = getAttribute(line, "v");

                        if (k != null && v != null) {
                            tmp.tags.put(k, v);
                        }
                    }
                }

            }

            resolveRelations();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void resolveRelations() {

        for (RelationTemp tmp : relationTempMap.values()) {
            OsmRelation relation = osmRelationMap.get(tmp.id);
            if (relation == null) continue;

            List<Member> members = new ArrayList<>();

            for (MemberRef mr : tmp.memberRefs) {
                OsmElement element = switch (mr.type) {
                    case "node" -> osmNodeMap.get(mr.ref);
                    case "way" -> osmWayMap.get(mr.ref);
                    case "relation" -> osmRelationMap.get(mr.ref);
                    default -> null;
                };

                if (element != null) {
                    members.add(new Member(element, mr.role));
                }
            }

            relation.setMembers(members);
            relation.setTags(tmp.tags);

        }
    }


    private static class MemberRef {
        String type;
        long ref;
        String role;

        MemberRef(String type, long ref, String role) {
            this.type = type;
            this.ref = ref;
            this.role = role;
        }
    }

    private static class RelationTemp {
        long id;
        List<MemberRef> memberRefs = new ArrayList<>();
        HashMap<String, String> tags = new HashMap<>();

        RelationTemp(long id) {
            this.id = id;
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
        String value = s.substring(valueStart, valueEnd);

        /*
        Decoding af xml enheder - til fejl OsmWayTagsAreCorrect
        - ved ikke om man kan gøre det anderledes?
        */
        value = value.replace("&amp;", "&");
        value = value.replace("&lt;", "<");
        value = value.replace("&gt;", ">");
        value = value.replace("&quot;", "\"");
        value = value.replace("&apos;", "'");

        return value;
    }

    public float getAttributeFloat(String s, String key) {
        String val = getAttribute(s, key);
        if (val == null) {
            return Float.NaN;
        }
        return Float.parseFloat(val);
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