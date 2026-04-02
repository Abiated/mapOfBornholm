package dk.itu.renderer;

import dk.itu.parser.Coordinate;
import dk.itu.parser.HeightCurve;
import dk.itu.parser.HeightCurveParser;
import dk.itu.parser.Parser;
import dk.itu.renderer.osm.OsmNode;
import dk.itu.renderer.osm.OsmWay;
import javafx.scene.canvas.GraphicsContext;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Renderer {
    Graphics2D gc;
    //float R = 6378137;

    Renderer(Graphics2D gc) {
        this.gc = gc;
    }

    Path2D getWayPath(OsmWay way, Integer strokeWidth) {
        // If the way has less than two nodes, draw nothing
        setWayColor(way);
        if (way.getNodes() == null || way.getNodes().size() < 2) {
            return new Path2D.Double();
        }

        if (way.getNodes().size() == 2){
            gc.setStroke(new BasicStroke(strokeWidth));

            OsmNode a = way.getNodes().get(0);
            float x1 = a.getLon();
            float y1 = a.getLat();

            OsmNode b = way.getNodes().get(1);
            float x2 = b.getLon();
            float y2 = b.getLat();
            Path2D.Double p = new Path2D.Double();
            p.moveTo(x1,y1);
            p.lineTo(x2,y2);
            gc.fill(p);
            return p;

        }

        if (way.getNodes().size() > 2) {
            Path2D.Double polygon = new Path2D.Double();
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
                gc.setStroke(new BasicStroke(0));
                polygon.closePath();
                gc.fill(polygon);
                return polygon;

            } else {
                gc.setStroke(new BasicStroke(strokeWidth));
                return polygon;
            }

        }
        return new Path2D.Double();
    }

    void setWayColor(OsmWay way) {
        Map<String, String> tags = way.getTags();

        // Default color
        gc.setColor(Color.LIGHT_GRAY);

        if ("coastline".equals(tags.get("natural"))) {
            gc.setColor(Color.YELLOW);

        } else if ("forest".equals(tags.get("landuse")) ||
                "forest".equals(tags.get("natural"))) {
            gc.setColor(Color.GREEN);

        } else if ("farmland".equals(tags.get("landuse")) ||
                "farmyard".equals(tags.get("landuse")) ||
                "orchard".equals(tags.get("landuse"))) {
            gc.setColor(Color.getHSBColor(90f / 360f, 0.7f, 0.7f));

        } else if ("residential".equals(tags.get("landuse")) ||
                "city".equals(tags.get("place")) ||
                "town".equals(tags.get("place"))) {
            gc.setColor(Color.GRAY);

        } else if ("path".equals(tags.get("highway"))) {
            gc.setColor(Color.DARK_GRAY);

        } else if ("water".equals(tags.get("natural")) ||
                "river".equals(tags.get("waterway"))) {
            gc.setColor(Color.BLUE);
        }
    }

    Path2D getHcBoundaryPath(HeightCurve curve, Integer strokeWidth) {
        gc.setStroke(new BasicStroke(strokeWidth));
        gc.setColor(Color.BLACK);
        Path2D hc = new Path2D.Double();
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
    Path2D getHcRegionPath(HeightCurve curve, Integer strokeWidth) {
        Path2D hc = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        hc.append(getHcBoundaryPath(curve,strokeWidth),false);
        for (HeightCurve heightcurve : curve.getChildren())
            hc.append(heightcurve.getBoundaryPath(),false);
        return hc;
    }
    /*

    float mercatorX(float lon) {
        return (float) (R * Math.toRadians(lon));
    }

    float mercatorY(float lat) {
        float rad = (float) Math.toRadians(lat);
        return (float) (R * Math.log(Math.tan(Math.PI / 4 + rad / 2)));
    }

     */
}
