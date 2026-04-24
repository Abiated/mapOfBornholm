package rendering;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class MapHandler {
    private final SuperAffine superAffine;
    private final Runnable interaction;
    private float screenX, screenY;
    private final PathFinder.EdgeWeightedDigraph graph;
    private final java.util.function.Supplier<java.util.Set<Integer>> submergedVertices;
    private final PathFinder.VehicleType vehicleType;
    private final interfaces.Data mapData;
    private final java.util.function.Consumer<java.util.List<PathFinder.DirectedEdge>> onRouteFound;

    private Double startLon, startLat;
    private boolean waitingForTarget = false;


    public MapHandler(SuperAffine superAffine, Runnable interaction,
                      PathFinder.EdgeWeightedDigraph graph,
                      java.util.function.Supplier<java.util.Set<Integer>> submergedVertices,
                      PathFinder.VehicleType vehicleType,
                      interfaces.Data mapData,
                      java.util.function.Consumer<java.util.List<PathFinder.DirectedEdge>> onRouteFound) {
        this.superAffine = superAffine;
        this.interaction = interaction;
        this.graph = graph;
        this.submergedVertices = submergedVertices;
        this.vehicleType = vehicleType;
        this.mapData = mapData;
        this.onRouteFound = onRouteFound;
    }

    //PAN ZOOM HELPER METHODS

    /**
     * Keeps track of the first mouse press in when panning.
     *
     * @param event The screen mouse press event that was triggered.
     */
    public void handleMousePressed(MouseEvent event) {
        this.screenX = (float) event.getX();
        this.screenY = (float) event.getY();
    }

    /**
     * Pans by following the mouse.
     *
     * @param event The screen mouse drag event that was triggered.
     */
    public void handleMouseClicked(MouseEvent event) {
        // ignore if it was a drag
        if (Math.abs(event.getX() - screenX) > 5 || Math.abs(event.getY() - screenY) > 5) return;

        double[] lonLat = screenToLonLat(event.getX(), event.getY(), mapData);

        if (!waitingForTarget) {
            startLon = lonLat[0];
            startLat = lonLat[1];
            waitingForTarget = true;
            System.out.println("Start set: " + startLon + ", " + startLat);
        } else {
            waitingForTarget = false;

            //If were using nearest vertex? Need to add lats and lons in edgeweightedDigraph
            int from   = graph.nearestVertex(startLon, startLat);
            int target = graph.nearestVertex(lonLat[0], lonLat[1]);

            PathFinder.Dijkstra dijkstra = new PathFinder.Dijkstra(
                    graph, from, target, vehicleType, submergedVertices.get());

            if (dijkstra.hasPathTo(target)) {
                onRouteFound.accept(dijkstra.pathTo(target));
            } else {
                System.out.println("No path found.");
            }
        }
    }

    /*
    public void handleMouseDragged(MouseEvent event) {
        float dx = (float)(event.getX() - screenX);
        float dy = (float)(event.getY() - screenY);
        screenX = (float) event.getX();
        screenY = (float) event.getY();
        superAffine.prependTranslation(dx, dy);
        interaction.run();
    }
    */

    private double[] screenToLonLat(double x, double y, interfaces.Data d) {
        int WIDTH = DrawingApp.getWIDTH();
        int HEIGHT = DrawingApp.getHEIGHT();
        float p = 20;
        float c = (float) Math.cos(Math.toRadians((d.getMinLat() + d.getMaxLat()) / 2));
        float w = (d.getMaxLon() - d.getMinLon()) * c;
        float h = d.getMaxLat() - d.getMinLat();
        float k = Math.min((WIDTH - 2 * p) / w, (HEIGHT - 2 * p) / h);

        java.awt.geom.AffineTransform t = new java.awt.geom.AffineTransform();
        t.translate(p + (WIDTH - 2 * p - w * k) / 2, p + (HEIGHT - 2 * p - h * k) / 2 + h * k);
        t.scale(k, -k);
        t.scale(c, 1);
        t.translate(-d.getMinLon(), -d.getMinLat());

        java.awt.geom.AffineTransform combined = new java.awt.geom.AffineTransform(superAffine);
        combined.concatenate(t);

        try {
            double[] screen = {x, y};
            double[] world  = new double[2];
            combined.inverseTransform(screen, 0, world, 0, 1);
            return world; // [lon, lat]
        } catch (Exception e) {
            return new double[]{0, 0};
        }
    }

    /**
     * Zooms in-and-out around the mouse.
     *
     * @param event The screen scroll event that was triggered.
     */
    public void handleScroll(ScrollEvent event) {
        float zoom = (float) (event.getDeltaY() > 0 ? 1.05 : 1 / 1.05);
        superAffine
                .prependTranslation((float) -event.getX(), (float) -event.getY())
                .prependScale(zoom, zoom)
                .prependTranslation((float) event.getX(), (float) event.getY());

        interaction.run();
    }

    /**
     * Re-centers the screen against some world lat/lon bounds.
     * The function will add the projection transformation of 0.56 to the bounds.
     *
     * @param bounds An array in the format {minLon, minLat, maxLon, maxLat}.
     */
    public void reCenter(float[] bounds, int height) {
        float scale = height / (bounds[3] - bounds[1]);
        superAffine
                .reset()
                .prependTranslation(
                        (float) (-0.56 * bounds[0]),
                        bounds[3]
                )
                .prependScale(
                        scale,
                        scale
                );
    }

}
