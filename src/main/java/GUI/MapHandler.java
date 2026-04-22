package GUI;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class MapHandler {
    private final SuperAffine superAffine;
    private final Runnable interaction;
    private float screenX, screenY;

    public MapHandler(SuperAffine superAffine, Runnable interaction) {
        this.superAffine = superAffine;
        this.interaction = interaction;
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
    public void handleMouseDragged(MouseEvent event) {
        float dx = (float) (event.getX() - this.screenX), dy = (float) (event.getY() - this.screenY);
        superAffine.prependTranslation(dx, dy);
        handleMousePressed(event);

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

}
