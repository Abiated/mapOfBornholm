package dk.itu.renderer;

import dk.itu.parser.Parser;
import dk.itu.renderer.osm.OsmNode;
import dk.itu.renderer.osm.OsmWay;
import javafx.scene.canvas.GraphicsContext;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.HashSet;

public class Renderer {
    int width;
    int height;
    Graphics2D gc;
    Parser parser;
    float minLat;
    float minLon;
    float maxLat;
    float maxLon;
    float scaleX;
    float scaleY;

    Renderer(Graphics2D gc, Parser parser, int width, int height) {
        this.width = width;
        this.height = height;
        this.gc = gc;
        this.parser = parser;
        minLat = parser.getBoundingBox().get(0);
        minLon = parser.getBoundingBox().get(1);
        maxLat = parser.getBoundingBox().get(2);
        maxLon = parser.getBoundingBox().get(3);
        scaleX = width / (maxLon - minLon);
        scaleY = height / (maxLat - minLat);
    }

    void drawWay(OsmWay way, Integer strokeWidth) {
        // If the way has less than two nodes, draw nothing
        if (way.getNodes() == null || way.getNodes().size() < 2) return;

        if (way.getNodes().size() == 2){
            gc.setStroke(new BasicStroke(strokeWidth));

            OsmNode a = way.getNodes().get(0);
            float x1 = (a.getLon() - minLon) * scaleX;
            float y1 = (maxLat - a.getLat()) * scaleY;

            OsmNode b = way.getNodes().get(1);
            float x2 = (b.getLon() - minLon) * scaleX;
            float y2 = (maxLat - b.getLat()) * scaleY;

            Path2D.Double p = new Path2D.Double();
            p.moveTo(x1,y1);
            p.lineTo(x2,y2);
            gc.draw(p);
            return;

        }

        if (way.getNodes().size() > 2) {
            Path2D.Double polygon = new Path2D.Double();
            OsmNode first = way.getNodes().getFirst();
            float x1 = (first.getLon() - minLon) * scaleX;
            float y1 = (maxLat - first.getLat()) * scaleY;
            polygon.moveTo(x1,y1);

            for (int i = 1; i < way.getNodes().size(); i++) {
                OsmNode n = way.getNodes().get(i);
                float xn = (n.getLon() - minLon) * scaleX;
                float yn = (maxLat - n.getLat()) * scaleY;
                polygon.lineTo(xn,yn);
            }

            HashSet<String> tags = new HashSet<>(way.getTags().values());
            /*
            for (String tag : tags) {
                System.out.println("WAY:");
                System.out.println(tag);
            }
             */
            gc.setColor(Color.getHSBColor(123.0F,70.0F,70.0F));
            if (tags.contains("coastline"))
                gc.setColor(Color.yellow);
            if (tags.contains("forest"))
                gc.setColor(Color.GREEN);
            if (tags.contains("agricultural")||tags.contains("farm")||tags.contains("farmyard")||tags.contains("orchard"))
                gc.setColor(Color.getHSBColor(90.0F, 70.0F, 70.0F));
            if (tags.contains("city")||tags.contains("residential")||tags.contains("town"))
                gc.setColor(Color.gray);
            if (tags.contains("path"))
                gc.setColor(Color.DARK_GRAY);
            if (tags.contains("pond")||tags.contains("river"))
                gc.setColor(Color.blue);


            // chatGPT siger det ik er optimalt det her
            if(way.getNodes().getFirst().getId() == way.getNodes().getLast().getId()){
                gc.setStroke(new BasicStroke(0));
                polygon.closePath();
                gc.fill(polygon);

            } else {
                gc.setStroke(new BasicStroke(strokeWidth));
                gc.draw(polygon);
            }
            //

        }


    }
}
