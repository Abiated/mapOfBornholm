package rendering;

import com.fasterxml.jackson.databind.JsonNode;
<<<<<<< Updated upstream
import models.HeightCurve;
import models.OsmNode;
import models.OsmWay;

import java.awt.*;
import java.awt.geom.Path2D;
=======
import interfaces.Renderable;
import models.*;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
>>>>>>> Stashed changes
import java.util.Map;

public class Renderer {

    Graphics2D gc;
    JsonNode jn;

    Renderer(Graphics2D gc, JsonNode jn) {
        this.gc = gc;
        this.jn =jn;
    }
<<<<<<< Updated upstream

    Path2D getWayPath(OsmWay way) {
=======
    
    //OSMWAY RENDERING
    public Path2D getWayPath(OsmWay way) {
>>>>>>> Stashed changes
        // If the way has less than two nodes, draw nothing
        if (way.getNodes() == null || way.getNodes().size() < 2) {
            return new Path2D.Float();
        }

        if (way.getNodes().size() == 2){
            //gc.setStroke(new BasicStroke(strokeWidth));

            OsmNode a = way.getNodes().get(0);
            float x1 = a.getLon();
            float y1 = a.getLat();

            OsmNode b = way.getNodes().get(1);
            float x2 = b.getLon();
            float y2 = b.getLat();
            Path2D.Float p = new Path2D.Float();
            p.moveTo(x1,y1);
            p.lineTo(x2,y2);
            gc.fill(p);
            way.setCachedShape(p);
            return p;

        }

        if (way.getNodes().size() > 2) {
            Path2D.Float polygon = new Path2D.Float(Path2D.WIND_EVEN_ODD);
            OsmNode first = way.getNodes().getFirst();
            float x1 = first.getLon();
            float y1 = first.getLat();
            polygon.moveTo(x1,y1);

            for (int i = 1; i < way.getNodes().size(); i++) {
                OsmNode n = way.getNodes().get(i);
                float xn = n.getLon();
                float yn = n.getLat();
                polygon.lineTo(xn,yn);
            }

            // chatGPT siger det ik er optimalt det her
            if(way.getNodes().getFirst().getId() == way.getNodes().getLast().getId()){
                //gc.setStroke(new BasicStroke(0));
                polygon.closePath();
                //gc.fill(polygon);
                way.setCachedShape(polygon);
                return polygon;

            } else {
                //gc.setStroke(new BasicStroke(strokeWidth));
                way.setCachedShape(polygon);
                return polygon;
            }

        }
        return new Path2D.Float();
    }

<<<<<<< Updated upstream
    void setWayColor(OsmWay way, float strokeWidth) {
=======
    public void setColor(Renderable way, float strokeWidth) {
>>>>>>> Stashed changes
        //Map<String, String> tags = way.getTags();

        // Default color
        gc.setColor(Color.red);

<<<<<<< Updated upstream
        if(way.hasTag("natural", "coastline")) {
            gc.setColor(Color.yellow);
=======
        if(way.isType(wayType.COASTLINE)) {
            gc.setColor(Color.decode("#EDE8D0"));
            return;
>>>>>>> Stashed changes
        }
        if (way.isType(wayType.WATER)) {
            gc.setColor(Color.decode("#2b8cbe"));
            //gc.setStroke(new BasicStroke(strokeWidth));
            return;
        }
        if(way.isType(wayType.BUILDING)) {
            gc.setColor(Color.decode(jn.path("building").path("color").asText()));
            gc.setStroke(new BasicStroke(strokeWidth));
            return;
        }
<<<<<<< Updated upstream

        String key = null;
        if (way.isWayType(wayType.BUILDING)) key = "natural";
        else if (way.isWayType(wayType.NATURAL)) key = "natural";
        else if (way.isWayType(wayType.ROAD)) key = "highway";
        else if (way.isWayType(wayType.LANDUSE)) key = "landuse";
        else if (way.isWayType(wayType.SEAROUTE)) key = "searoute";
        else if (way.isWayType(wayType.WATER)) key = "water";
        else if (way.isWayType(wayType.AMENITIES)) key = "amenity";
        else if (way.isWayType(wayType.AEROWAYS)) key = "aeroway";
        else if (way.isWayType(wayType.MAN_MADE)) key = "man_made";
=======
        if (way.isType(wayType.SEAROUTE)) {
            gc.setColor(Color.decode(jn.path("searoute").path("color").asText()));
            gc.setStroke(new BasicStroke(strokeWidth * 1,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10.0f, new float[]{5.0f}, 0.0f));
            return;
        }

        String key = null;
        if (way.isType(wayType.BUILDING)) key = "natural";
        else if (way.isType(wayType.NATURAL)) key = "natural";
        else if (way.isType(wayType.ROAD)) key = "highway";
        else if (way.isType(wayType.LANDUSE)) key = "landuse";
        else if (way.isType(wayType.WATER)) key = "water";
        else if (way.isType(wayType.AMENITIES)) key = "amenity";
        else if (way.isType(wayType.AEROWAYS)) key = "aeroway";
        else if (way.isType(wayType.MAN_MADE)) key = "man_made";
>>>>>>> Stashed changes
        else key = "other";

        JsonNode jPath = jn.path(key).path(way.getValue());

        String color = jPath.isMissingNode() ? jn.path(key).path("defaultColor").asText()
                : jPath.path("color").asText();

        try {
            gc.setColor(Color.decode(color));
        } catch (NumberFormatException e) {
            gc.setColor(Color.LIGHT_GRAY);
            System.out.println(e.getMessage());
        }

        if(way.isType(wayType.ROAD)) {
            int width = jPath.isMissingNode() ? 1 : jPath.path("width").asInt();
            if(jPath.path("dashed").asBoolean()) {
                gc.setStroke(new BasicStroke(strokeWidth * width,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        10.0f, new float[]{5.0f}, 0.0f));
            } else gc.setStroke(new BasicStroke(strokeWidth * width));
        } else gc.setStroke(new BasicStroke(strokeWidth));
    }

<<<<<<< Updated upstream
    Path2D getHcBoundaryPath(HeightCurve curve) {
        Path2D hc = new Path2D.Float();
        // problemer opstår hvis heightcurve ikke er parsed
        float x1 = curve.getCoords().getFirst().getLon();
        float y1 = curve.getCoords().getFirst().getLat();
        hc.moveTo(x1,y1);
        for (int i = 1; i<curve.getCoords().size(); i++) {
            float x = curve.getCoords().get(i).getLon();
            float y = curve.getCoords().get(i).getLat();
            hc.lineTo(x,y);
=======
    //OSMRELATION RENDERING
    public void renderMultipolygon(OsmRelation relation, Shape projected) {
        if(relation.getTags() == null ||projected == null) return;
        
        Map<String, String> tags = relation.getTags();

        setColor(relation, 1);
        gc.fill(projected);
    }
    
    
    public Path2D buildMultiPolygon(OsmRelation relation) {
        if  (relation.getTags() == null) return null;
        if  (!relation.getTags().containsKey("type")) return null;
        if  (!relation.getTags().get("type").equals("multipolygon")) return null;
        if  (relation.getMembers() == null || relation.getMembers().isEmpty()) return null;

        List<OsmWay> outerWays = new ArrayList<>();
        List<OsmWay> innerWays = new ArrayList<>();

        for(Member member : relation.getMembers()) {
            if(!(member.getElement() instanceof OsmWay)) continue;
            OsmWay way = (OsmWay) member.getElement();

            if (way == null || way.getNodes() == null || way.getNodes().size() < 2) continue;
            //if ((way.getNodes().stream().anyMatch(n -> n == null))) //Needed? runtime...

            if(member.getRole().equals("outer")) outerWays.add(way);
            else if(member.getRole().equals("inner")) innerWays.add(way);
>>>>>>> Stashed changes
        }

        if (outerWays.isEmpty()) return null;

        Path2D results = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        for (OsmWay way : Stitcher.stitch(outerWays))
            results.append(getWayPath(way), false);
        for (OsmWay way : Stitcher.stitch(innerWays))
            results.append(getWayPath(way), false);


        return results.getBounds2D().isEmpty() ? null : results;
    }

//    public Path2D getHcBoundaryPath(HeightCurve curve) {
//        Path2D hc = new Path2D.Float();
//        // problemer opstår hvis heightcurve ikke er parsed
//        float x1 = curve.getCoords().getFirst().getLon();
//        float y1 = curve.getCoords().getFirst().getLat();
//        hc.moveTo(x1,y1);
//        for (int i = 1; i<curve.getCoords().size(); i++) {
//            float x = curve.getCoords().get(i).getLon();
//            float y = curve.getCoords().get(i).getLat();
//            hc.lineTo(x,y);
//        }
//        return hc;
//    }
//    Path2D getHcRegionPath(HeightCurve curve) {
//        Path2D hc = new Path2D.Float(Path2D.WIND_EVEN_ODD);
//        hc.append(getHcBoundaryPath(curve),false);
//        for (HeightCurve heightcurve : curve.getChildren())
//            hc.append(heightcurve.getBoundaryPath(),false);
//        return hc;
//    }
}

