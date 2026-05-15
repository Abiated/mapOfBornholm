package parser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import interfaces.Data;
import interfaces.IParser;
import models.OsmNode;
import models.OsmRelation;
import models.OsmWay;
import models.Member;

public class Parser implements IParser, Data, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    //private final String fileName;
    private final List<Float> boundingBox = new ArrayList<>();
    private final HashMap<Long, OsmNode> osmNodeMap = new HashMap<>();
    private final HashMap<Long, OsmWay> osmWayMap = new HashMap<>();
    private final HashMap<Long, OsmRelation> osmRelationMap = new HashMap<>();
    private final transient InputStream inputStream;

    public Parser(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void parse() {
        try {
            //Check if inputstream is there
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found " + inputStream);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line;

            //Way attributes
            long ID = -1;
            List<OsmNode> wayNodeRef = null;
            List<Member> membersList = null;
            HashMap<String, String> tags = null;
            boolean inWay = false;
            boolean inRelation = false;


            while ((line = br.readLine()) != null) {
                //System.out.println(line);

                /*––––––––– BOUNDS –––––––––*/
                if (line.contains("<bounds")) {
                    //Converts string to float, and adds to boundingBox in given order.
                    float minlat = getAttributeFloat(line, "minlat");
                    boundingBox.add(minlat);

                    float minlon = getAttributeFloat(line, "minlon");
                    boundingBox.add(minlon);

                    float maxlat = getAttributeFloat(line, "maxlat");
                    boundingBox.add(maxlat);

                    float maxlon = getAttributeFloat(line, "maxlon");
                    boundingBox.add(maxlon);
                }

                /*––––––––– NODES –––––––––*/
                if (line.contains("<node")) {
                    //Declares a value for  ID, lat and lon. And initializes for given value
                    long noteId = getAttributeLong(line, "id");
                    float lat = getAttributeFloat(line, "lat");
                    float lon = getAttributeFloat(line, "lon");

                    //Puts key-value pair with id as key and instance of OsmNode as value
                    OsmNode node = new OsmNode(noteId, lat, lon);
                    osmNodeMap.put(noteId, node);
                }

                /*––––––––– WAY –––––––––*/
                if (line.contains("<way")) {
                    //Instances all relevant variables
                    ID = getAttributeLong(line, "id");
                    wayNodeRef = new ArrayList<>();
                    tags = new HashMap<>();
                    inWay = true;

                    //Puts a key-value pair for ID in the map
//                    if (!osmWayMap.containsKey(ID)) {
//                        osmWayMap.put(ID, new OsmWay(ID, wayNodeRef, tags));
//                    }
                }
                /*––––––––– WAY CONTENT –––––––––*/
                else if (inWay && line.contains("<nd")) {
                    //Saves <nd to a list
                    long ref = getAttributeLong(line, "ref");
                    /*if(osmNodeMap.get(ref) != null)*/ wayNodeRef.add(osmNodeMap.get(ref));

                    //Updates map
//                    osmWayMap.put(ID, new OsmWay(ID, wayNodeRef, tags));
                } else if (inWay && line.contains("<tag")) {
                    //Puts key-value pair for the "way"
                    String k = getAttribute(line, "k");
                    String v = getAttribute(line, "v");

                    //Check that the map is instantiated
                    if (tags != null) tags.put(k, v);

                    //Updates map
//                    osmWayMap.put(ID, new OsmWay(ID, wayNodeRef, tags));
                }
                /*––––––––– WAY END –––––––––*/
                else if (inWay && line.contains("</way>")) {
                    //Resets the variables for the next "way"
                    osmWayMap.put(ID, new OsmWay(ID, wayNodeRef, tags));
                    ID = -1;
                    wayNodeRef = null;
                    tags = null;
                    inWay = false;
                }

                /*––––––––– RELATION –––––––––*/
                if (line.contains("<relation")) {
                    ID = getAttributeLong(line, "id");
                    membersList = new ArrayList<>();
                    tags = new HashMap<>();
                    inRelation = true;

                    //Puts a key-value pair for ID in the map
                    if (!osmRelationMap.containsKey(ID)) {
                        osmRelationMap.put(ID, new OsmRelation(ID, membersList, tags));
                    }
                }
                /*––––––––– RELATION CONTENT –––––––––*/
                else if (inRelation && line.contains("<tag")) {
                    String k = getAttribute(line, "k");
                    String v = getAttribute(line, "v");

                    //Check that the map is instantiated
                    if (tags != null) tags.put(k, v);

                    //Updates map
                    osmRelationMap.put(ID, new OsmRelation(ID, membersList, tags));
                } else if (inRelation && line.contains("<member")) {

                    String type = getAttribute(line, "type");
                    String role = getAttribute(line, "role");
                    long memberRef = getAttributeLong(line, "ref");

                    if (type.equals("way")) {
                        OsmWay memberType = osmWayMap.get(memberRef);

                        if (memberType != null) membersList.add(new Member(memberType, role));
                    } else if (type.equals("node")) {
                        OsmNode memberType = osmNodeMap.get(memberRef);

                        if (memberType != null) membersList.add(new Member(memberType, role));
                    } else if (type.equals("relation")) {
                        OsmRelation memberType = osmRelationMap.get(memberRef);

                        if (memberType == null) {
                            memberType = new OsmRelation(memberRef, new ArrayList<>(), new HashMap<>()); //Placeholder
                            osmRelationMap.put(memberRef, memberType);
                        }
                        membersList.add(new Member(memberType, role));
                    }

                    //Updates map
                    osmRelationMap.put(ID, new OsmRelation(ID, membersList, tags));
                }
                /*––––––––– RELATION END –––––––––*/
                else if (inRelation && line.equals("</relation>")) {
                    tags = null;
                    membersList = null;
                    inRelation = false;
                    ID = -1;
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

    @Override
    public float getMinLat() {
        return boundingBox.getFirst();
    }

    @Override
    public float getMaxLat() {
        return boundingBox.get(2);
    }

    @Override
    public float getMinLon() {
        return boundingBox.get(1);
    }

    @Override
    public float getMaxLon() {
        return boundingBox.get(3);
    }
}
