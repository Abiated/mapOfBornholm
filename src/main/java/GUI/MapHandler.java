package GUI;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import java.awt.geom.Point2D;

public class MapHandler {
    private final SuperAffine superAffine;
    private final Runnable interaction;
    private float screenX, screenY;
    private final PathFinder.EdgeWeightedDigraph graph;
    private final java.util.function.Supplier<java.util.Set<Integer>> submergedVertices;
    private final java.util.function.Supplier<PathFinder.VehicleType> vehicleTypeSupplier;
    private final rendering.MapProjection projection;
    private final java.util.function.BiConsumer<java.util.List<PathFinder.DirectedEdge>, Float> onRouteFound;;

    private Double startLon, startLat;
    private Double targetLon, targetLat;
    private boolean waitingForTarget = false;

    public MapHandler(SuperAffine superAffine, Runnable interaction) {
        this(superAffine, interaction, null, null, null, null, null);
    }

    public MapHandler(SuperAffine superAffine, Runnable interaction,
                      PathFinder.EdgeWeightedDigraph graph,
                      java.util.function.Supplier<java.util.Set<Integer>> submergedVertices,
                      java.util.function.Supplier<PathFinder.VehicleType> vehicleTypeSupplier,
                      rendering.MapProjection projection,
                      java.util.function.BiConsumer<java.util.List<PathFinder.DirectedEdge>, Float>  onRouteFound) {
        this.superAffine = superAffine;
        this.interaction = interaction;
        this.graph = graph;
        this.submergedVertices = submergedVertices;
        this.vehicleTypeSupplier = vehicleTypeSupplier;
        this.projection = projection;
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
        if (graph == null || projection == null || onRouteFound == null) {
            return;
        }

        // ignore if it was a drag
        if (Math.abs(event.getX() - screenX) > 5 || Math.abs(event.getY() - screenY) > 5) return;

        //double[] lonLat = screenToLonLat(event.getX(), event.getY(), projection);
        Point2D world = projection.screenToWorld(event.getX(), event.getY());

        if (!waitingForTarget) {
            startLon = world.getX();
            startLat = world.getY();
            targetLon = null;
            targetLat = null;
            waitingForTarget = true;
            System.out.println("Start set: " + startLon + ", " + startLat);
        } else {
            waitingForTarget = false;
            targetLon = world.getX();
            targetLat = world.getY();
            calculateRoute();
        }
    }



    public void handleMouseDragged(MouseEvent event) {
        float dx = (float)(event.getX() - screenX);
        float dy = (float)(event.getY() - screenY);
        screenX = (float) event.getX();
        screenY = (float) event.getY();
        superAffine.prependTranslation(dx, dy);
        interaction.run();
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


    // ROUTE FINDING METHODS
    public void recalculateRoute() {
        if (startLon == null || startLat == null || targetLon == null || targetLat == null) {
            return;
        }
        calculateRoute();
    }

    private void calculateRoute() {
        PathFinder.VehicleType vehicleType = vehicleTypeSupplier != null ? vehicleTypeSupplier.get() : PathFinder.VehicleType.CAR;
        int from = graph.nearestVertex(startLon, startLat, vehicleType);
        int target = graph.nearestVertex(targetLon, targetLat, vehicleType);

        PathFinder.Dijkstra dijkstra = new PathFinder.Dijkstra(graph, from, target, vehicleType, submergedVertices != null ? submergedVertices.get() : java.util.Collections.emptySet());

        if (dijkstra.hasPathTo(target)) {
            onRouteFound.accept(dijkstra.pathTo(target), dijkstra.distTo(target));
        } else {
            System.out.println("No path found.");
            onRouteFound.accept(null, Float.POSITIVE_INFINITY);
        }
    }

}
