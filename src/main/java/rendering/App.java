package rendering;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import GUI.BottomControls;
import GUI.MapHandler;
import GUI.SuperAffine;
import GUI.UpperControls;
import interfaces.Data;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.*;
import parser.HcParser;
import parser.Parser;

public class App extends DrawingApp {

    private static final int WIDTH = getWIDTH();
    private static final int HEIGHT = getHEIGHT();
    private static final Color WATER_COLOR = Color.decode("#2b8cbe");
    private static final SuperAffine superAffine = new SuperAffine();

    private Float SEA_LEVEL = 0.0F;
    private HeightCurveData HCdata;
    private Parser OSMdata;
    private HeightCurve sea;
    private Data projectionSource;
    private HashMap<Long, OsmWay> ways;
    private final EnumMap<wayType, List<OsmWay>> waysByType = new EnumMap<>(wayType.class);
    private JsonNode jRoot;
    private Renderer renderer;
    private BottomControls bottomControls;


    @Override
    public void start(Stage stage) {

        //---------DATA PARSING---------//
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open map file (.zip), HeightCurve files (.hc) or OpenStreetMap files (.osm)"); //Message when selecting files
        //Filters which files can be chosen for the FileChooser
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All map files", "*.hc", "*.osm", "*.zip", "*.bin")
        );

        //Chosen file, returns null if none are chosen
        File chosen = chooser.showOpenDialog(stage);

        if (chosen == null) {
            // No file chosen – loads default
            loadDefaultData();
        } else if (chosen.getName().endsWith(".zip")) {
            //Zip file chosen
            loadCache(chosen, "zip");
        } else if (chosen.getName().endsWith(".hc")) {
            //Hc file chosen
            loadCache(chosen, "hc");
        } else if (chosen.getName().endsWith(".osm")) {
            //Osm file chosen
            loadCache(chosen, "osm");
        } else if (chosen.getName().endsWith(".bin")) {
            if (chosen.getName().endsWith("zip.bin")) loadCache(chosen, "zip.bin");
            if (chosen.getName().endsWith("osm.bin")) loadCache(chosen, "osm.bin");
            if (chosen.getName().endsWith("hc.bin")) loadCache(chosen, "hc.bin");

        }

        //Fiks man burde genre kunne nøjes med at køerer en af filerne igennem
//        if (HCdata == null || OSMdata == null) {
//            throw new IllegalStateException("Both height curve data and OSM data must be loaded before rendering");
//        }

        //Get sea from HCdata and ways
        if (HCdata != null) sea = HCdata.sea;
        if (OSMdata != null) ways = OSMdata.getOsmWayMap();

        //Classify wayTypes
        if (OSMdata != null){
            for (wayType type : wayType.values()) {
                if (type == wayType.COASTLINE) continue;
                waysByType.put(type, new ArrayList<>());
            }

            waysByType.put(wayType.COASTLINE, CoastlineStitcher.stitch(ways));

            for (OsmWay way : ways.values()) {
                if (way.hasTag("location", "underground")) continue;
                else if (way.isWater()) waysByType.get(wayType.WATER).add(way);
                else if (way.isSeaRoute()) waysByType.get(wayType.SEAROUTE).add(way);
                else if (way.hasTag("natural", "coastline")) continue;
                else if (way.isNatural()) waysByType.get(wayType.NATURAL).add(way);
                else if (way.isLandues()) waysByType.get(wayType.LANDUSE).add(way);
                else if (way.isBuilding()) waysByType.get(wayType.BUILDING).add(way);
                else if (way.isRoad())
                    waysByType.get(wayType.ROAD).add(way);
                else if (way.isLeisure()) waysByType.get(wayType.LEISURE).add(way);
    //            else {
    //                if(!(way.hasTag("highway", "proposed") && way.getTags().get("seamark:type") != null))
    //                    waysByType.get(wayType.OTHER).add(way);
    //            }
            }
    }

        //---------CONFIG PARSING---------//
        //For now default json file maybe later there can be a choice.
        try {
            InputStream is = getClass().getResourceAsStream("/config.json");
            if (is == null) System.out.println("File not found");

            jRoot = (new ObjectMapper()).readTree(is);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        //---------GUI CODE---------//
        //Creating SeaSlider and ScaleBar with our bottomControls
        bottomControls = new BottomControls(((observable, oldValue, newValue) -> {
            SEA_LEVEL = newValue.floatValue();
            draw();
        }));

        UpperControls upperControls = new UpperControls();

        BorderPane root = new BorderPane();
        root.setBottom(bottomControls);
        root.setTop(upperControls);

        //Stage title is set to fileName
        if (chosen != null) {
            String fileName = chosen.getName().substring(0, chosen.getName().lastIndexOf('.'));
            stage.setTitle("Topographic map of " + fileName);
        } else {
            stage.setTitle("Topographic map of default island");
        }

        stage.setResizable(false);
        //Panning and zooming functions called from class MapHandler
        MapHandler interactionHandler = new MapHandler(superAffine, this::draw);

        //BorderPane mouseEventComponent = new BorderPane();
        root.setOnMousePressed(interactionHandler::handleMousePressed);
        root.setOnMouseDragged(interactionHandler::handleMouseDragged);
        root.setOnScroll(interactionHandler::handleScroll);

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

    //FOR DATA PARSER
    private void saveCache(File cache) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(cache)))) {
            oos.writeObject(OSMdata);
            oos.writeObject(HCdata);
            System.out.println("Saved cache: " + cache.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadCache(File chosen, String type) {
        //Checks file type, of not .bin it searches for binary counterpart
        File cache = null;
        switch (type) {
            case "zip":
                cache = new File(chosen.getAbsolutePath().replace(".zip", "zip.bin"));
                break;
            case "osm":
                cache = new File(chosen.getAbsolutePath().replace(".osm", "osm.bin"));
                break;
            case "hc":
                cache = new File(chosen.getAbsolutePath().replace(".hc", "hc.bin"));
                break;
            default:
                cache = new File(chosen.getAbsolutePath());
        }

        if(cache == null) return;

        if (cache.exists()) {
            try (ObjectInput ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(cache)))) {
                    OSMdata = (Parser) ois.readObject();
                    HCdata = (HeightCurveData) ois.readObject();
                //System.out.println("Running cache");
            } catch (Exception e) {
                System.err.println("Cache failed, reparsing: " + e.getMessage());
            }
        } else {

            //Checks which file to load of no binary file exist
            if (type.contains("bin")) throw new IllegalArgumentException("No such file exist");
            if(type.equals("zip")) {
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
               } catch (IOException e) {
                   throw new RuntimeException(e);
               }
           } else if(type.equals("osm")) {
               OSMdata = new Parser(openFile(chosen));
               OSMdata.parse();
           } else if (type.equals("hc")) {
               HCdata = new HcParser(openFile(chosen)).parse();
           }

            saveCache(cache);
        }
    }

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
    public void draw() {

        Graphics2D gc = getNewGraphicsContext();
        renderer = new Renderer(gc, jRoot);

        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //Background
        gc.setColor(WATER_COLOR);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        //Zoom
        projectionSource = (OSMdata != null) ? OSMdata : HCdata;
        float zoom = getZoom(projectionSource);
        bottomControls.update(zoom);

        //Midlertidig løsning måske?
        zoom = zoom / 1000; //Making the zoom level manageable for rendering with zoom
        System.out.println("Manageable zoom level: " + zoom);

        float strokeWidth = (float) (1.0 / Math.sqrt(Math.abs(
                superAffine.getScaleX() * superAffine.getScaleY()
        )));
        strokeWidth = Math.clamp(strokeWidth, 0.5f, 1.0f);

        if(HCdata != null) {
            sea.resetSubmerged();
            sea.submerge(SEA_LEVEL);
        }

        //Is only drawn when osm data is parsed
        if (OSMdata != null) {
            //TODO: ADD FERRYWAYS, AEROWAYS AND AMENITIES, ASWELL AS MULTIPOLYGON FORREST
            //Draws waterways, lakes, ponds and so on
            drawLayer(wayType.WATER, gc, strokeWidth, zoom);
            //Draws ferry routes and other sea route
            drawLayer(wayType.SEAROUTE, gc, strokeWidth, zoom);
            //Draws polygon for coastline
            drawLayer(wayType.COASTLINE, gc, strokeWidth, zoom);
            //Draws natural ways
            drawLayer(wayType.NATURAL, gc, strokeWidth, zoom);
            //Draws landuse ways
            drawLayer(wayType.LANDUSE, gc, strokeWidth, zoom);
            //Draws roads
            drawLayer(wayType.ROAD, gc, strokeWidth, zoom);
            //Draws buildings
            drawLayer(wayType.BUILDING, gc, strokeWidth, zoom);

            //TODO: LEFTEROVERS
            //Draws leisure ways, needs some fixing
            //drawLayer(wayType.LEISURE, gc, strokeWidth, zoom);
            //Draws otherwise non categorized ways
            //drawLayer(wayType.OTHER, gc, strokeWidth, zoom);
        }


        if (HCdata != null) {
            drawHeightCurvesRecursive(sea.getChildren(), gc, projectionSource);
        }


        gc.dispose();
        render();
    }

    private void drawHeightCurvesRecursive(List<HeightCurve> curves, Graphics2D gc, Data projectionSource) {
        for (HeightCurve curve : curves) {
            if(OSMdata != null) gc.setColor(curve.getFillColor(SEA_LEVEL, false));
            else gc.setColor(curve.getFillColor(SEA_LEVEL, true));
            gc.fill(project(curve.getRegionPath(), projectionSource));
            drawHeightCurvesRecursive(curve.getChildren(), gc, projectionSource);
        }
    }

    public float getZoomLevel(OsmWay way) {
        String key = null;
        if (way.isNatural()) key = "natural";
        else if (way.isRoad()) key = "highway";
        else if (way.isLandues()) key = "landuse";
        else if (way.isLeisure()) {
            key = "leisure";
            return (float) jRoot.path(key).path("zoom").asDouble();
        } else if (way.isBuilding()) {
            key = "building";
            return (float) jRoot.path(key).path("zoom").asDouble();
        } else return 0;
        JsonNode jPath = jRoot.path(key).path(way.getValue()).path("zoom");

        if (jPath.isMissingNode()) return 0;

        return (float) jPath.asDouble();
    }

    private void drawLayer(wayType type, Graphics2D gc, float sw, float zoom) {
        for (OsmWay way : waysByType.get(type)) {
            if (type != wayType.WATER && zoom < getZoomLevel(way)) continue;

            Shape projected = project(renderer.getWayPath(way), projectionSource);
            renderer.setWayColor(way, sw);
            if (way.getNodes().getFirst().getId() == way.getNodes().getLast().getId()
                    && !(way.isRoad() && !way.hasTag("highway", "rest_area"))
                    && !way.isSeaRoute())
                gc.fill(projected);
            else gc.draw(projected);

        }
    }

    private static Shape project(Shape s, Data d) {
        float p = 20, c = (float) Math.cos(Math.toRadians((d.getMinLat() + d.getMaxLat()) / 2));
        float w = (d.getMaxLon() - d.getMinLon()) * c, h = d.getMaxLat() - d.getMinLat();
        float k = Math.min((WIDTH - 2 * p) / w, (HEIGHT - 2 * p) / h);
        AffineTransform t = new AffineTransform();
        t.translate(p + (WIDTH - 2 * p - w * k) / 2, p + (HEIGHT - 2 * p - h * k) / 2 + h * k);
        t.scale(k, -k);
        t.scale(c, 1);
        t.translate(-d.getMinLon(), -d.getMinLat());

        AffineTransform combined = new AffineTransform(superAffine);
        combined.concatenate(t);

        return combined.createTransformedShape(s);
    }

    private static float getZoom(Data d) {
        float p = 20, c = (float) Math.cos(Math.toRadians((d.getMinLat() + d.getMaxLat()) / 2));
        float w = (d.getMaxLon() - d.getMinLon()) * c, h = d.getMaxLat() - d.getMinLat();
        float k = Math.min((WIDTH - 2 * p) / w, (HEIGHT - 2 * p) / h);

        float superAffineZoom = (float) Math.sqrt(superAffine.getDeterminant());

        return k * superAffineZoom;
    }

    public float getSEALEVEL() {
        return SEA_LEVEL;
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
