package dk.itu.renderer.osm;

import dk.itu.renderer.Drawable;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.Map;

public class OsmWay extends OsmElement implements Drawable {

    private final List<OsmNode> nodes;
    private final Map<String, String> tags;

    public OsmWay(long id, List<OsmNode> nodes, Map<String, String> tags) {
        super(id);
        this.nodes = nodes;
        this.tags = tags;
        double xHighest = 0;
        double yHighest = 0;
        for(OsmNode Node : nodes){
            if (Node.getLat() > yHighest){
                yHighest = Node.getLat();
            }
            if (Node.getLon() > xHighest){
                xHighest = Node.getLon();
            }
        }
        this.setArea(xHighest*yHighest);

    }

    public List<OsmNode> getNodes() {
        return nodes;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public void drawForTest(Graphics2D gc, Color color, Integer strokeWidth) {
        // If the way has less than two nodes, draw nothing
        /*
        if (nodes == null || nodes.size() < 2) return;

        gc.setColor(color);

        if (nodes.size() == 2){
            gc.setStroke(new BasicStroke(strokeWidth));
            OsmNode a = nodes.get(0);
            OsmNode b = nodes.get(1);

            Path2D.Double p = new Path2D.Double();
            p.moveTo(a.getLon(), a.getLat());
            p.lineTo(b.getLon(), b.getLat());
            gc.draw(p);
            return;

        }

        if (nodes.size() > 2) {
            Path2D.Double polygon = new Path2D.Double();
            OsmNode first = nodes.getFirst();
            polygon.moveTo(first.getLon(), first.getLat());

            for (int i = 1; i < nodes.size(); i++) {
                OsmNode n = nodes.get(i);
                polygon.lineTo(n.getLon(), n.getLat());
            }

            if(nodes.getFirst().getId() == nodes.getLast().getId()){
                gc.setStroke(new BasicStroke(0));
                polygon.closePath();
                gc.fill(polygon);

            } else {
                gc.setStroke(new BasicStroke(strokeWidth));
                gc.draw(polygon);
            }

        }



         */
    }

}
