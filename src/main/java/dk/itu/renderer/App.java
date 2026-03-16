package dk.itu.renderer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import dk.itu.parser.HeightCurveData;
import dk.itu.parser.HeightCurveParser;
import dk.itu.parser.Parser;
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

    private static final double SEA_LEVEL = 0.0; // (default: 0.0)
    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private double screenX = 0;
    private double screenY = 0;
    private final SuperAffine superAffine = new SuperAffine();
    private final List<Drawable> drawableList = new ArrayList<>();

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

    @Override
    public void start(Stage stage) {
        /* BASIC COMPONENT SETUP */
        stage.setTitle("Drawing App"); // Sets the title
        stage.setResizable(false); // Set non-resizable
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);

        BorderPane mouseEventComponent = new BorderPane();
        mouseEventComponent.setOnMousePressed(this::handleMousePressed);
        mouseEventComponent.setOnMouseDragged(this::handleMouseDragged);
        mouseEventComponent.setOnScroll(this::handleScroll);


        stage.setScene(
                new Scene(
                        new StackPane( // Root component => StackPane stacks components (like stacking cards)
                                this.imageView,mouseEventComponent// Our primary component from DrawingApp
                                // ... <= Components can be rendered on top of our map
                        ), getWIDTH(), getHEIGHT() // Sets height
                )
        );
        stage.show(); // Sets the stage as visible


        List<OsmNode> list = new ArrayList<>();
        list.add(new OsmNode(1,10,10));
        list.add(new OsmNode(2,20,30));
        list.add(new OsmNode(3,30,10));
        list.add(new OsmNode( 4,10,10 ));
        OsmWay way = new OsmWay(4,list,new HashMap<String,String>());
        Drawable e = (Drawable)way;
        this.drawableList.add(e);

        /* DRAWING */
        // Example with line across screen

        Parser parser = new Parser("bondeholm.osm");
        parser.parse();


        // parser.getOsmWayMap().get(4120948).getNodes().get(0).getLat() = 30;

        List<OsmElement> waylist = new ArrayList<OsmElement>(parser.getOsmWayMap().values());

        for (OsmElement element : waylist) {
            this.drawableList.add(element);
        }



        this.draw();

        this.render(); // This will actually show what has been drawn
    }

    public void draw() {
        // This provides our main drawing API
        Graphics2D gc = bufferedImage.createGraphics();
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
        // Uncomment if drawing actual OSM data
        DrawingUtils.applyTransformation(gc);
        // IMPORTANT - IF DRAWING BONDEHOLM, ENABLE THIS AND MULTIPLY LONGITUDE BY 0.56 AND LATITUDE BY -1 (the reason why will be explained later in the course)
        // Here we can do any pre-processing such a setting the background or transformations
        gc.setColor(Color.BLUE); // Sets the color
        gc.fillRect(0, 0, getWIDTH(), getHEIGHT()); // Fills the rectangle with the set color
        // gc.setTransform(superAffine);



        /*
        HeightCurveData data = Tunoe.create();

        HeightCurve sea = data.sea;
        sea.resetSubmerged();
        sea.submerge(SEA_LEVEL);


         */
        // Draws our drawables
        for (Drawable drawable : drawableList) {
            // In the actual project, color and strokeWidth arguments won't be in the draw method signature
            // Color and strokeWidth are only there for test purposes in the assignment
            drawable.drawForTest(gc, Color.BLACK, 1);

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

        int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
        System.arraycopy(pixels, 0, pixelBuffer.getBuffer().array(), 0, pixels.length);
        imageView.setImage(new WritableImage(pixelBuffer));
    }
    private void handleMousePressed(MouseEvent event) {
        this.screenX = event.getX();
        this.screenY = event.getY();
    }

    /**
     * Pans by following the mouse.
     * @param event The screen mouse drag event that was triggered.
     */
    private void handleMouseDragged(MouseEvent event) {
        double dx = event.getX() - this.screenX, dy = event.getY() - this.screenY;
        superAffine.prependTranslation(dx, dy);
        handleMousePressed(event);

        draw();
        render();
    }

    /**
     * Zooms in-and-out around the mouse.
     * @param event The screen scroll event that was triggered.
     */
    private void handleScroll(ScrollEvent event) {
        double zoom = event.getDeltaY() > 0 ? 1.05 : 1/1.05;
        superAffine
                .prependTranslation(-event.getX(), -event.getY())
                .prependScale(zoom, zoom)
                .prependTranslation(event.getX(), event.getY());

        draw();
        render();
    }

    /**
     * Re-centers the screen against some world lat/lon bounds.
     * The function will add the projection transformation of 0.56 to the bounds.
     * @param bounds An array in the format {minLon, minLat, maxLon, maxLat}.
     */
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

    private static Shape project(Shape s, HeightCurveData d) {
        double p = 20, c = Math.cos(Math.toRadians((d.minLat + d.maxLat) / 2));
        double w = (d.maxLon - d.minLon) * c, h = d.maxLat - d.minLat;
        double k = Math.min((WIDTH - 2 * p) / w, (HEIGHT - 2 * p) / h);
        AffineTransform t = new AffineTransform();
        t.translate(p + (WIDTH - 2 * p - w * k) / 2, p + (HEIGHT - 2 * p - h * k) / 2 + h * k);
        t.scale(k, -k);
        t.scale(c, 1);
        t.translate(-d.minLon, -d.minLat);
        return t.createTransformedShape(s);
    }
}
