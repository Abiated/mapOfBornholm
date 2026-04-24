package rendering;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import GUI.ScaleBar;
import interfaces.Data;
import interfaces.InsertableInRtree;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.*;
import parser.HcParser;
import parser.Parser;
import spatialDatastructures.Rtree;

public class App extends DrawingApp {

    private static final int WIDTH = getWIDTH();
    private static final int HEIGHT = getHEIGHT();
    private float screenX = 0;
    private float screenY = 0;
    private static final Color WATER_COLOR = Color.decode("#2b8cbe");
    private static final SuperAffine superAffine = new SuperAffine();

    private Float SEA_LEVEL = 0.0F;
    private HeightCurveData HCdata;
    private Parser OSMdata;
    private HeightCurve sea;
    private HashMap<Long, OsmWay> ways;
    // waysByType er nu et map der mapper hver waytype til en array af lister af ways,
    // hvor index 0 i arrayet indeholder listen af ways der skal vises ved lavt zoom osv
    // index 2 indeholder alle ways af typen da alt skal vises ved højt zoom
    private final EnumMap<wayType, List<OsmWay>[]> waysByType = new EnumMap<>(wayType.class);
    private JsonNode jRoot;
    private Renderer renderer;
    private ScaleBar scale = new ScaleBar();
    private EnumMap<wayType,Rtree[]> RtreesByType = new EnumMap<>(wayType.class);
    private Rtree HeightCurveRTree;
    private AffineTransform t;
    private int CutOffToRtree = 7;


    @Override
    public void start(Stage stage) {

        //---------DATA PARSING---------//
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open map file (.zip) or HeightCurve files (.hc)"); //Message when selecting files
        //Filters which files can be chosen for the FileChooser
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All map files", "*.hc", "*.osm", "*.zip")
//                new FileChooser.ExtensionFilter("HeightCurve files", "*.hc"),
//                new FileChooser.ExtensionFilter("OpenStreetMap files", "*.osm"),
//                new FileChooser.ExtensionFilter("Zip files", "*.zip")
        );

        //Chosen file, returns null if none are chosen
        File chosen = chooser.showOpenDialog(stage);

        if (chosen == null) {
            loadDefaultData();
        } else if (chosen.getName().endsWith(".hc")) {
            //Hc file chosen
            HCdata = new HcParser(openFile(chosen)).parse();
            //OSMdata = loadDefaultOsmData();
        } else if (chosen.getName().endsWith(".osm")) {
            //Osm file chosen
            OSMdata = new Parser(openFile(chosen));
            OSMdata.parse();
            //HCdata = loadDefaultHeightCurveData();
        } else if (chosen.getName().endsWith(".zip")) {
            // Zip file chosen
            try {
                ZipInputStream zip = new ZipInputStream(new FileInputStream(chosen));
                ZipEntry entry;
                boolean loadedHc = false;
                boolean loadedOsm = false;

                while ((entry = zip.getNextEntry()) != null) {
                    if (entry.isDirectory() || entry.getName().contains("__MACOSX") || entry.getName().contains(".DS_Store")) {
                        zip.closeEntry();
                        continue;
                    }

                    byte[] bytes = zip.readAllBytes();

                    if (entry.getName().endsWith(".hc")) {
                        HCdata = new HcParser(new ByteArrayInputStream(bytes)).parse();
                        loadedHc = true;
                    }
                    if (entry.getName().endsWith(".osm")) {
                        OSMdata = new Parser(new ByteArrayInputStream(bytes));
                        OSMdata.parse();
                        loadedOsm = true;
                    }
                    zip.closeEntry();
                }

                if (!loadedHc || !loadedOsm) {
                    throw new IllegalArgumentException("Zip file must contain both .hc and .osm files");
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        //Fiks man burde genre kunne nøjes med at køerer en af filerne igennem
        if (HCdata == null || OSMdata == null) {
            throw new IllegalStateException("Both height curve data and OSM data must be loaded before rendering");
        }

        //Get sea from HCdata and ways // HEIGHTCURVES FÅR START TIL AT TAGE MARKANT LÆNGERE TID
        sea = HCdata.sea;
        ways = OSMdata.getOsmWayMap();
        t = getProjectionTransform(OSMdata);

        //---------CONFIG PARSING---------//
        //For now default json file maybe later there can be a choice.
        try {
            InputStream is = getClass().getResourceAsStream("/config.json");
            if (is == null) System.out.println("File not found");

            jRoot = (new ObjectMapper()).readTree(is);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        // coastline, water, searoute, other har ikke zoom værdier.

        for (wayType type : wayType.values()) {
            if(type == wayType.COASTLINE) continue;
            List[] listarray = new List[] {new ArrayList<OsmWay>(), new ArrayList<OsmWay>(), new ArrayList<OsmWay>()};
            waysByType.put(type, listarray);
        }

        List[] listarray = new List[] {new ArrayList<OsmWay>(CoastlineStitcher.stitch(ways)), new ArrayList<OsmWay>(), new ArrayList<OsmWay>()};
        waysByType.put(wayType.COASTLINE, listarray);

        // index 0: 0 < zoom < 10
        // index 1: 0 < zoom < 40
        // index 2: 0 < zoom

        for(OsmWay way : ways.values()) {
            if (way.hasTag("location", "underground")) continue;
            else if(way.isWater()) {
                if (getZoomLevel(way) < 10)
                    waysByType.get(wayType.WATER)[0].add(way);
                if (getZoomLevel(way) < 40)
                    waysByType.get(wayType.WATER)[1].add(way);
                waysByType.get(wayType.WATER)[2].add(way);
            }
            else if (way.isSeaRoute()) {
                if (getZoomLevel(way) < 10)
                    waysByType.get(wayType.SEAROUTE)[0].add(way);
                if (getZoomLevel(way) < 40)
                    waysByType.get(wayType.SEAROUTE)[1].add(way);
                waysByType.get(wayType.SEAROUTE)[2].add(way);
            }
            else if(way.hasTag("natural", "coastline")) continue;
            else if(way.isNatural()) {
                if (getZoomLevel(way) < 10)
                    waysByType.get(wayType.NATURAL)[0].add(way);
                if (getZoomLevel(way) < 40)
                    waysByType.get(wayType.NATURAL)[1].add(way);
                waysByType.get(wayType.NATURAL)[2].add(way);
            }
            else if(way.isLandues()) {
                if (getZoomLevel(way) < 10)
                    waysByType.get(wayType.LANDUSE)[0].add(way);
                if (getZoomLevel(way) < 40)
                    waysByType.get(wayType.LANDUSE)[1].add(way);
                waysByType.get(wayType.LANDUSE)[2].add(way);
            }
            else if(way.isBuilding()) {
                if (getZoomLevel(way) < 10)
                    waysByType.get(wayType.BUILDING)[0].add(way);
                if (getZoomLevel(way) < 40)
                    waysByType.get(wayType.BUILDING)[1].add(way);
                waysByType.get(wayType.BUILDING)[2].add(way);
            }
            else if(way.isRoad()) {
//                if(hasOutOfBoundsNode(way)) {
//                    System.out.println("Out of bounds road: " + way.getTags());
//                    continue; // skip it
//                }  waysByType.get(wayType.ROAD).add(way); }
                if (getZoomLevel(way) < 10)
                    waysByType.get(wayType.ROAD)[0].add(way);
                if (getZoomLevel(way) < 40)
                    waysByType.get(wayType.ROAD)[1].add(way);
                waysByType.get(wayType.ROAD)[2].add(way);
            }
            else if(way.isLeisure()) {
                if (getZoomLevel(way) < 10)
                    waysByType.get(wayType.LEISURE)[0].add(way);
                if (getZoomLevel(way) < 40)
                    waysByType.get(wayType.LEISURE)[1].add(way);
                waysByType.get(wayType.LEISURE)[2].add(way);
            }
//            else {
//                if(!(way.hasTag("highway", "proposed") && way.getTags().get("seamark:type") != null))
//                    waysByType.get(wayType.OTHER).add(way);
//            }
        }

        // CREATE R TREES
        List<HeightCurve> curves = new ArrayList<>(HCdata.curves);
        curves.remove(sea);
        HeightCurveRTree = new Rtree(new ArrayList<InsertableInRtree>(curves),32);
        Rtree[] one = new Rtree[] {new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.COASTLINE)[0]),32),new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.COASTLINE)[0]),32),new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.COASTLINE)[0]),32)};
        RtreesByType.put(wayType.COASTLINE,one);
        if (!waysByType.get(wayType.WATER)[0].isEmpty()) {
            Rtree[] two = new Rtree[]{new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.WATER)[0]), 32), new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.WATER)[0]), 32), new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.WATER)[0]), 32)};
            RtreesByType.put(wayType.WATER, two);
        }
        if (!waysByType.get(wayType.SEAROUTE)[0].isEmpty()) {
            Rtree[] three = new Rtree[]{new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.SEAROUTE)[0]), 32), new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.SEAROUTE)[0]), 32), new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.SEAROUTE)[0]), 32)};
            RtreesByType.put(wayType.SEAROUTE, three);
        }
        if (!waysByType.get(wayType.NATURAL)[0].isEmpty()) {
            Rtree[] four = new Rtree[]{new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.NATURAL)[0]), 32), new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.NATURAL)[1]), 32), new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.NATURAL)[2]), 32)};
            RtreesByType.put(wayType.NATURAL, four);
        }
        if (!waysByType.get(wayType.LANDUSE)[0].isEmpty()) {
            Rtree[] five = new Rtree[]{new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.LANDUSE)[0]), 32), new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.LANDUSE)[1]), 32), new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.LANDUSE)[2]), 32)};
            RtreesByType.put(wayType.LANDUSE, five);
        }
        if (!waysByType.get(wayType.BUILDING)[0].isEmpty()) {
            Rtree[] six = new Rtree[]{new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.BUILDING)[0]), 32), new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.BUILDING)[1]), 32), new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.BUILDING)[2]), 32)};
            RtreesByType.put(wayType.BUILDING, six);
        }
        if (!waysByType.get(wayType.ROAD)[0].isEmpty()) {
            Rtree[] seven = new Rtree[]{new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.ROAD)[0]), 32), new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.ROAD)[1]), 32), new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.ROAD)[2]), 32)};
            RtreesByType.put(wayType.ROAD, seven);
        }
        if (!waysByType.get(wayType.LEISURE)[0].isEmpty()) {
            Rtree[] eight = new Rtree[]{new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.LEISURE)[0]), 32), new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.LEISURE)[1]), 32), new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.LEISURE)[2]), 32)};
            RtreesByType.put(wayType.LEISURE, eight);
        }

        //---------GUI CODE---------//
        //TODO: MOVE THIS INTO IT'S OWN CLASSES TO CLEAN THIS UP
        //Slider to control the SEA_LEVEL
        //Slider(minValue, maxValue, startValue)
        Slider slider = new Slider(0, 20, 0);
        //Shows markings on slider
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);

        //Label for sea level slider
        Label sliderLabel = new Label("Sea level: 0.0 m");

        //Listener, that runs when the slider is moved
        slider.valueProperty().addListener((obs, oldValue, newValue) -> {

            //Updates to new sea level
            SEA_LEVEL = newValue.floatValue();
            //Updates label for sea level slider, with new sea level
            sliderLabel.setText(String.format("Sea level: %.1f m", SEA_LEVEL));
            //Calls draw, to redraw the map
            draw();
        });

        //HBox that puts slider and sliderLabel side by side with 12px padding
        HBox controls = new HBox(12.0, slider, sliderLabel);
        //Styles controls with 10px padding and alignment
        controls.setStyle("-fx-padding: 10; -fx-alignment: center-left;");

        HBox scaleBar = new HBox(scale);
        scaleBar.setStyle("-fx-padding: 20; -fx-alignment: center-right;");

        BorderPane bottom = new BorderPane();
        bottom.setRight(scaleBar);
        bottom.setLeft(controls);

        BorderPane root = new BorderPane();
        root.setBottom(bottom);

        //Stage title is set to fileName
        if (chosen != null) {
            String fileName = chosen.getName().substring(0, chosen.getName().lastIndexOf('.'));
            stage.setTitle("Topographic map of " + fileName);
        } else {
            stage.setTitle("Topographic map of default island");
        }

        stage.setResizable(false);

        //BorderPane mouseEventComponent = new BorderPane();
        root.setOnMousePressed(this::handleMousePressed);
        root.setOnMouseDragged(this::handleMouseDragged);
        root.setOnScroll(this::handleScroll);

        stage.setScene(
                new Scene(
                        new StackPane(
                                this.imageView,
                                root
                        ),
                        WIDTH, HEIGHT
                )
        );

        stage.show();

        draw();
    }

    //PAN ZOOM HELPER METHODS

    /**
     * Keeps track of the first mouse press in when panning.
     *
     * @param event The screen mouse press event that was triggered.
     */
    private void handleMousePressed(MouseEvent event) {
        this.screenX = (float) event.getX();
        this.screenY = (float) event.getY();
    }

    /**
     * Pans by following the mouse.
     *
     * @param event The screen mouse drag event that was triggered.
     */
    private void handleMouseDragged(MouseEvent event) {
        float dx = (float) (event.getX() - this.screenX), dy = (float) (event.getY() - this.screenY);
        superAffine.prependTranslation(dx, dy);
        handleMousePressed(event);

        draw();
    }

    /**
     * Zooms in-and-out around the mouse.
     *
     * @param event The screen scroll event that was triggered.
     */
    private void handleScroll(ScrollEvent event) {
        float zoom = (float) (event.getDeltaY() > 0 ? 1.05 : 1 / 1.05);
        superAffine
                .prependTranslation((float) -event.getX(), (float) -event.getY())
                .prependScale(zoom, zoom)
                .prependTranslation((float) event.getX(), (float) event.getY());

        draw();
    }

    /**
     * Re-centers the screen against some world lat/lon bounds.
     * The function will add the projection transformation of 0.56 to the bounds.
     *
     * @param bounds An array in the format {minLon, minLat, maxLon, maxLat}.
     */
    public void reCenter(float[] bounds) {
        float scale = getHEIGHT() / (bounds[3] - bounds[1]);
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

    //FOR DATA PARSER
    private void loadDefaultData() {
        HCdata = loadDefaultHeightCurveData();
        OSMdata = loadDefaultOsmData();
    }

    private HeightCurveData loadDefaultHeightCurveData() {
        InputStream inputStream = getClass().getResourceAsStream("/hc-osm-files/tuna/tuna.hc");
        return new HcParser(inputStream).parse();
    }

    private Parser loadDefaultOsmData() {
        InputStream inputStream = getClass().getResourceAsStream("/hc-osm-files/tuna/tuna.osm");
        Parser parser = new Parser(inputStream);
        parser.parse();
        return parser;
    }

    private InputStream openFile(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //FOR DRAWING AND RENDERING
    private void draw() {

        Graphics2D gc = getNewGraphicsContext();
        renderer = new Renderer(gc, jRoot);

        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //Background
        gc.setColor(WATER_COLOR);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        //Zoom
        float zoom = getZoom(OSMdata);
        scale.update(zoom);

        //Midlertidig løsning måske?
        zoom = zoom/1000; //Making the zoom level manageable for rendering with zoom
        System.out.println("Manageable zoom level: " + zoom);

        //TODO: REMOVE THIS SHIT AND MAKE A ABSOLUT STROKEWIDTH FOR ALLE ELEMENTS
        float strokeWidth = (float)(1.0 / Math.sqrt(Math.abs(
                superAffine.getScaleX() * superAffine.getScaleY()
        )));
        strokeWidth = Math.clamp(strokeWidth, 0.5f, 1.0f);

        sea.resetSubmerged();
        sea.submerge(SEA_LEVEL);

        //Draws waterways, lakes, ponds and so on
        drawLayer(wayType.WATER, gc, strokeWidth, zoom);
        //Draws ferry routes and other sea route
        drawLayer(wayType.SEAROUTE, gc, strokeWidth, zoom);
        //Draws polygon for coastline
        drawLayer(wayType.COASTLINE, gc, strokeWidth, zoom);
        //Draws natural ways
        //if (zoom >= getZoomLevelByType(wayType.NATURAL))
            drawLayer(wayType.NATURAL, gc, strokeWidth, zoom);
        //Draws otherwise non categorized ways
        //drawLayer(wayType.OTHER, gc, strokeWidth, zoom);
        //Draws landuse ways
        //if (zoom >= getZoomLevelByType(wayType.LANDUSE))
            drawLayer(wayType.LANDUSE, gc, strokeWidth, zoom);
        //Draws leisure ways, needs some fixing
        //if (zoom >= getZoomLevelByType(wayType.LEISURE))
            //drawLayer(wayType.LEISURE, gc, strokeWidth, zoom);

        //Draw Layer 4 - heightCurves raw paths
        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        List<InsertableInRtree> curves;
        if (zoom>CutOffToRtree) {
            curves = HeightCurveRTree.search(getViewPortBoundingBox(OSMdata));
        }
        else curves = new ArrayList<>(HCdata.curves);
        for (InsertableInRtree element : curves) {
            HeightCurve curve = (HeightCurve) element;
            if (curve == sea) {
                continue;
            }
            gc.setColor(curve.getFillColor(SEA_LEVEL));
            gc.fill(project(curve.getRegionPath(), t));
        }

//        //Draws waterways, lakes, ponds and so on
//        drawLayer(wayType.WATER, gc, strokeWidth, zoom);
        //Draws roads
        //if (zoom >= getZoomLevelByType(wayType.ROAD))
            drawLayer(wayType.ROAD, gc, strokeWidth, zoom);
        //Draws buildings
        //if (zoom >= getZoomLevelByType(wayType.BUILDING))
            drawLayer(wayType.BUILDING, gc, strokeWidth, zoom);

        //Draw Layer 7
//        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        gc.setColor(new Color(0, 0, 0, 60)); //Semi transparent
//        gc.setStroke(new BasicStroke(strokeWidth));
//        for (HeightCurve curve : HCdata.curves) {
//            if (curve == sea) continue;
//            gc.draw(project(curve.getBoundaryPath(), OSMdata));
//        }

        gc.dispose();
        render();
    }

    public float getZoomLevelByType(wayType type) {
        String key = null;
        if (type == wayType.NATURAL) return 0;
        else if (type == wayType.ROAD) return 0;
        else if (type == wayType.LANDUSE) return 0;
        else if (type == wayType.LEISURE) {
            return (float) jRoot.path("leisure").path("zoom").asDouble();
        }
        else if (type == wayType.BUILDING) {
            return (float) jRoot.path("building").path("zoom").asDouble();
        }
        else return 0;
    }

    public float getZoomLevel(OsmWay way) {
        String key = null;
        if (way.isNatural()) key = "natural";
        else if (way.isRoad()) key = "highway";
        else if (way.isLandues()) key = "landuse";
        else if (way.isLeisure()) {
            key = "leisure";
            return (float) jRoot.path(key).path("zoom").asDouble();
        }
        else if (way.isBuilding()) {
            key = "building";
            return (float) jRoot.path(key).path("zoom").asDouble();
        }
        else return 0;
        JsonNode jPath = jRoot.path(key).path(way.getValue()).path("zoom");

        if (jPath.isMissingNode()) return 0;

        return (float) jPath.asDouble();
    }

    private void drawLayer(wayType type, Graphics2D gc, float sw, float zoom) {
        List<InsertableInRtree> ways;
        int i;
        if (zoom < 10) i = 0;
        else if (zoom < 40) i = 1;
        else i = 2;
        if (zoom>CutOffToRtree) {
            if (RtreesByType.get(type) == null) return;
            ways = RtreesByType.get(type)[2].search(getViewPortBoundingBox(OSMdata));
        }
        else {
            if (waysByType.get(type)[2].isEmpty()) return;
            ways = new ArrayList<>(waysByType.get(type)[2]);
        }
        int x = 0;
        for (InsertableInRtree element : ways) {
            x++;
            OsmWay way = (OsmWay) element;
            if (type != wayType.WATER && zoom < getZoomLevel(way)) continue;
            Shape projected = project(renderer.getWayPath(way), t);
            renderer.setWayColor(way, sw);
            if (way.getNodes().getFirst().getId() == way.getNodes().getLast().getId() && !(way.isRoad() && !way.hasTag("highway", "rest_area")) && !way.isSeaRoute())
                gc.fill(projected);
            else gc.draw(projected);
        }
        System.out.println(x + " elements iterated in layer");
    }

    private static AffineTransform getProjectionTransform(Data d) {
        float p = 20, c = (float) Math.cos(Math.toRadians((d.getMinLat() + d.getMaxLat()) / 2));
        float w = (d.getMaxLon() - d.getMinLon()) * c, h = d.getMaxLat() - d.getMinLat();
        float k = Math.min((WIDTH - 2 * p) / w, (HEIGHT - 2 * p) / h);
        AffineTransform t = new AffineTransform();
        t.translate(p + (WIDTH - 2 * p - w * k) / 2, p + (HEIGHT - 2 * p - h * k) / 2 + h * k);
        t.scale(k, -k);
        t.scale(c, 1);
        t.translate(-d.getMinLon(), -d.getMinLat());
        return t;
    }

    private static Shape project(Shape s, AffineTransform t) {
        AffineTransform combined = new AffineTransform(superAffine);
        combined.concatenate(t);

        return combined.createTransformedShape(s);
    }

    private static Point2D reverseProject(float sx, float sy, AffineTransform t) {
        AffineTransform combined = new AffineTransform(superAffine);
        combined.concatenate(t);

        // taget fra chatGPT
        try {
            AffineTransform inverse = combined.createInverse();

            Point2D screenPt = new Point2D.Double(sx, sy);
            Point2D worldPt = new Point2D.Double();

            inverse.transform(screenPt, worldPt);

            return worldPt; // (lon, lat)
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException("Transform not invertible", e);
        }
    }

    private float[] getViewPortBoundingBox(Data d) {
        Point2D topleft = reverseProject(0,0,t);
        Point2D topright = reverseProject(WIDTH,0,t);
        Point2D bottomright = reverseProject(WIDTH,HEIGHT,t);
        Point2D bottomleft = reverseProject(0,HEIGHT,t);

        float minX = (float) Math.min(bottomleft.getX(),Math.min(bottomright.getX(),Math.min(topleft.getX(),topright.getX())));
        float maxX = (float) Math.max(bottomleft.getX(),Math.max(bottomright.getX(),Math.max(topleft.getX(),topright.getX())));
        float minY = (float) Math.min(bottomleft.getY(),Math.min(bottomright.getY(),Math.min(topleft.getY(),topright.getY())));
        float maxY = (float) Math.max(bottomleft.getY(),Math.max(bottomright.getY(),Math.max(topleft.getY(),topright.getY())));

        return new float[] {minX,maxX,minY,maxY};
    }

    private static float getZoom(Data d) {
        float p = 20, c = (float) Math.cos(Math.toRadians((d.getMinLat() + d.getMaxLat()) / 2));
        float w = (d.getMaxLon() - d.getMinLon()) * c, h = d.getMaxLat() - d.getMinLat();
        float k = Math.min((WIDTH - 2 * p) / w, (HEIGHT - 2 * p) / h);

        float superAffineZoom = (float) Math.sqrt(superAffine.getDeterminant());

        return k * superAffineZoom;
    }

    //Debug midlertidig
    private boolean hasOutOfBoundsNode(OsmWay way) {
        for (OsmNode node : way.getNodes()) {
            if (node.getLat() < OSMdata.getMinLat() || node.getLat() > OSMdata.getMaxLat() ||
                    node.getLon() < OSMdata.getMinLon() || node.getLon() > OSMdata.getMaxLon()) {
                return true;
            }
        }
        return false;
    }
}
