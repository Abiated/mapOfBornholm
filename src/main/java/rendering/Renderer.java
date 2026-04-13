package rendering;

import com.fasterxml.jackson.databind.JsonNode;
import models.HeightCurve;
import models.OsmNode;
import models.OsmWay;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Map;

public class Renderer {

    Graphics2D gc;
    JsonNode jn;

    Renderer(Graphics2D gc, JsonNode jn) {
        this.gc = gc;
        this.jn =jn;
    }

    Path2D getWayPath(OsmWay way) {
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
            return p;

        }

        if (way.getNodes().size() > 2) {
            Path2D.Float polygon = new Path2D.Float();
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
                return polygon;

            } else {
                //gc.setStroke(new BasicStroke(strokeWidth));
                return polygon;
            }

        }
        return new Path2D.Float();
    }

    void setWayColor(OsmWay way, float strokeWidth) {
        //Map<String, String> tags = way.getTags();

        // Default color
        gc.setColor(Color.LIGHT_GRAY);

        if(way.hasTag("natural", "coastline")) {
            gc.setColor(Color.yellow);
        }
        if (way.isWater()) {
            gc.setColor(Color.decode(jn.path("waterColor").asText()));
            gc.setStroke(new BasicStroke(strokeWidth));
            return;
        }
        if(way.isBuilding()) {
            gc.setColor(Color.decode(jn.path("building").path("color").asText()));
            gc.setStroke(new BasicStroke(strokeWidth));
            return;
        }

        String key = null;
        if (way.isNatural()) key = "natural";
        else if (way.isRoad()) key = "highway";
        else if (way.isLandues()) key = "landuse";
        else if (way.isLeisure()) key = "leisure";
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

        if(way.isRoad()) {
            int width = jPath.isMissingNode() ? 1 : jPath.path("width").asInt();
            gc.setStroke(new BasicStroke(strokeWidth * width));
        } else gc.setStroke(new BasicStroke(strokeWidth));
    }

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
        }
        return hc;
    }
    Path2D getHcRegionPath(HeightCurve curve) {
        Path2D hc = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        hc.append(getHcBoundaryPath(curve),false);
        for (HeightCurve heightcurve : curve.getChildren())
            hc.append(heightcurve.getBoundaryPath(),false);
        return hc;
    }
}

