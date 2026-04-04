package dk.itu.renderer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import dk.itu.parser.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;

import dk.itu.renderer.osm.*;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends DrawingApp {

    private static final float SEA_LEVEL = 5.0F; // (default: 0.0)
    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private float screenX = 0;
    private float screenY = 0;
    private final SuperAffine superAffine = new SuperAffine();

    private final PixelBuffer<IntBuffer> pixelBuffer = new PixelBuffer<>(
            WIDTH,
            HEIGHT,
            IntBuffer.allocate(WIDTH * HEIGHT),
            PixelFormat.getIntArgbPreInstance()
    );

    private final BufferedImage bufferedImage = new BufferedImage(
            WIDTH,
            HEIGHT,
            BufferedImage.TYPE_INT_ARGB
    );

    private final ImageView imageView = new ImageView();
    Graphics2D gc = bufferedImage.createGraphics();
    List<OsmWay> ways = new ArrayList<>();
    List<HeightCurve> heightcurves = new ArrayList<>();
    private Renderer renderer;
    private Parser parser;
    private HeightCurveParser HcParser;
    private HeightCurveData HcData;

    @Override
    public void start(Stage stage) {
        /* BASIC COMPONENT SETUP */
        stage.setTitle("Drawing App"); // Sets the title
        stage.setResizable(false); // Set non-resizable
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);

        /*
        BorderPane mouseEventComponent = new BorderPane();
        mouseEventComponent.setOnMousePressed(this::handleMousePressed);
        mouseEventComponent.setOnMouseDragged(this::handleMouseDragged);
        mouseEventComponent.setOnScroll(this::handleScroll);
         */
        stage.setScene(
                new Scene(
                        new StackPane( // Root component => StackPane stacks components (like stacking cards)
                                this.imageView//,mouseEventComponent// Our primary component from DrawingApp
                                // ... <= Components can be rendered on top of our map
                        ), getWIDTH(), getHEIGHT() // Sets height
                )
        );
        stage.show(); // Sets the stage as visible

        parser = new Parser("tunoe.osm");
        parser.parse();
        List<OsmElement> waylist = new ArrayList<OsmElement>(parser.getOsmWayMap().values());
        DrawingUtils.sortOsmElementsByArea(waylist);
        HcParser = new HeightCurveParser("tunoe.hc");
        HcData = HcParser.parse();
        renderer = new Renderer(gc);
        for (OsmElement element : waylist) {
            OsmWay wayski = (OsmWay)element;
            ways.add(wayski);
        }
        HcData.getSea().resetSubmerged();
        HcData.getSea().submerge(SEA_LEVEL);
        for (HeightCurve curve : HcData.getCurves()) {
            if (!curve.getCoords().isEmpty())
                heightcurves.add(curve);
        }

        this.draw();

        this.render(); // This will actually show what has been drawn
    }

    public void draw() {
        // This provides our main drawing API
        /*
        gc.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF
        );
        gc.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE
        );
        gc.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

         */
        // Here we can do any pre-processing such a setting the background or transformations
        gc.setColor(Color.BLUE); // Sets the color
        gc.fillRect(0, 0, getWIDTH(), getHEIGHT()); // Fills the rectangle with the set color
        //gc.setTransform(superAffine);


        for (OsmWay way : ways) {
            // In the actual project, color and strokeWidth arguments won't be in the draw method signature
            // Color and strokeWidth are only there for test purposes in the assignment
            Shape projected = project(renderer.getWayPath(way),parser);
            renderer.setWayColor(way);
            if (way.getNodes().getFirst()==way.getNodes().getLast())
                gc.fill(projected);
            gc.draw(projected);
        }

        for (HeightCurve heightcurve : heightcurves) {
            Shape projected = project(renderer.getHcRegionPath(heightcurve),parser);
            if (heightcurve.getFillColor(SEA_LEVEL) != null) {
                gc.setColor(heightcurve.getFillColor(SEA_LEVEL));
                gc.fill(projected);
            }
            else
                gc.setColor(Color.BLACK);
            gc.draw(projected);
        }
/*
        for (HeightCurve curve : data.curves) {
            if (curve == sea) {
                continue;
            }
            gc.setColor(curve.getFillColor(SEA_LEVEL));
            gc.fill(project(curve.getRegionPath(), data));
        }
        for (HeightCurve curve : data.curves) {
            if (curve == sea) {
                continue;
            }
            gc.setColor(Color.BLACK);
            gc.draw(project(curve.getBoundaryPath(), data));
        }

         */
        // This takes the BufferedImage, and gets the pixels representation
        int[] pixels = ((DataBufferInt) this.bufferedImage.getRaster().getDataBuffer()).getData();
        // This copies our pixels into the PixelBuffer
        System.arraycopy(pixels, 0, this.pixelBuffer.getBuffer().array(), 0, pixels.length);
        // This updates our graphical component with the buffer
        this.imageView.setImage(new WritableImage(this.pixelBuffer));
    }

    /*
    private void handleMousePressed(MouseEvent event) {
        this.screenX = event.getX();
        this.screenY = event.getY();
    }

     */

    /**
     * Pans by following the mouse.
     * @param event The screen mouse drag event that was triggered.
     */
    /*
    private void handleMouseDragged(MouseEvent event) {
        double dx = event.getX() - this.screenX, dy = event.getY() - this.screenY;
        superAffine.prependTranslation(dx, dy);
        //handleMousePressed(event);

        draw();
        render();
    }


     * Zooms in-and-out around the mouse.
     * @param event The screen scroll event that was triggered.

    private void handleScroll(ScrollEvent event) {
        double zoom = event.getDeltaY() > 0 ? 1.05 : 1/1.05;
        superAffine
                .prependTranslation(-event.getX(), -event.getY())
                .prependScale(zoom, zoom)
                .prependTranslation(event.getX(), event.getY());

        draw();
        render();
    }


     * Re-centers the screen against some world lat/lon bounds.
     * The function will add the projection transformation of 0.56 to the bounds.
     * @param bounds An array in the format {minLon, minLat, maxLon, maxLat}.

    public void reCenter(double[] bounds) {
        double scale = getHEIGHT() / (bounds[3] - bounds[1]);
        superAffine
                .reset()
                .prependTranslation(
                        -0.56 * bounds[0],
                        bounds[3]
                )
                .prependScale(
                        scale,
                        scale
                );
    }

     */

    private static Shape project(Shape s, Parser parser) {
        float minLat = parser.getBoundingBox().get(0);
        float minLon = parser.getBoundingBox().get(1);
        float maxLat = parser.getBoundingBox().get(2);
        float maxLon = parser.getBoundingBox().get(3);
        float p = 20, c = (float) Math.cos(Math.toRadians((minLat + maxLat) / 2));
        float w = (maxLon - minLon) * c, h = maxLat - minLat;
        float k = Math.min((WIDTH - 2 * p) / w, (HEIGHT - 2 * p) / h);
        AffineTransform t = new AffineTransform();
        t.translate(p + (WIDTH - 2 * p - w * k) / 2, p + (HEIGHT - 2 * p - h * k) / 2 + h * k);
        t.scale(k, -k);
        t.scale(c, 1);
        t.translate(-minLon, -minLat);
        return t.createTransformedShape(s);
    }

}
