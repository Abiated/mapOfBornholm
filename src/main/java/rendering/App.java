package rendering;

import GUI.BottomControls;
import GUI.MapHandler;
import GUI.SuperAffine;
import GUI.UpperControls;
import PathFinder.DirectedEdge;
import PathFinder.EdgeWeightedDigraph;
import PathFinder.SubmergedVertexFinder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import interfaces.Data;
import interfaces.InsertableInRtree;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import models.*;
import parser.HcParser;
import parser.Parser;
import spatialDatastructures.Rtree;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class App extends DrawingApp {

    private static final int WIDTH = getWIDTH();
    private static final int HEIGHT = getHEIGHT();
    private float screenX = 0;
    private float screenY = 0;
    private static final Color WATER_COLOR = Color.decode("#2b8cbe");
    private static final Color ROUTE_COLOR = Color.decode("#d7301f");
    private static final SuperAffine superAffine = new SuperAffine();

    private Float SEA_LEVEL = 0.0F;
    private HeightCurveData HCdata;
    private Parser OSMdata;
    private HeightCurve sea;
    private HeightCurve outer;
    private Data projectionSource;
    private HashMap<Long, OsmWay> ways;
    private List<HeightCurve> curves;
    private final EnumMap<wayType, List<OsmWay>> waysByType = new EnumMap<>(wayType.class);
    private JsonNode jRoot;
    private Renderer renderer;
    private BottomControls bottomControls;
    private UpperControls upperControls;
    private EdgeWeightedDigraph graph;
    private MapHandler interactionHandler;
    private List<DirectedEdge> currentRoute = new ArrayList<>();
    private Set<Integer> submergedVertices = new HashSet<>();

    private EnumMap<wayType,Rtree> RtreesByType = new EnumMap<>(wayType.class);
    private Rtree HeightCurveRTree;
    private AffineTransform t;
    private int CutOffToRtree = 7;


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
        if (OSMdata != null) t = getProjectionTransform(OSMdata);
        else t = getProjectionTransform(HCdata);

        //---------CONFIG PARSING---------//
        //For now default json file maybe later there can be a choice.
        try {
            InputStream is = getClass().getResourceAsStream("/config.json");
            if (is == null) System.out.println("File not found");

            jRoot = (new ObjectMapper()).readTree(is);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        if (OSMdata != null) graph = EdgeWeightedDigraph.constructGraph(OSMdata, jRoot);

        // coastline, water, searoute, other har ikke zoom værdier.

        if (OSMdata != null) {
            for (wayType type : wayType.values()) {
                if (type == wayType.COASTLINE) continue;
                waysByType.put(type, new ArrayList<>());
            }

            waysByType.put(wayType.COASTLINE, CoastlineStitcher.stitch(ways));

            for (OsmWay way : ways.values()) {
                if (way.isWayType(wayType.NON_DRAWING)) continue;
                else if (way.isWayType(wayType.COASTLINE)) continue;
                else if (way.isWayType(wayType.WATER)) waysByType.get(wayType.WATER).add(way);
                else if (way.isWayType(wayType.SEAROUTE)) waysByType.get(wayType.SEAROUTE).add(way);
                else if (way.isWayType(wayType.NATURAL)) waysByType.get(wayType.NATURAL).add(way);
                else if (way.isWayType(wayType.LANDUSE)) waysByType.get(wayType.LANDUSE).add(way);
                else if (way.isWayType(wayType.BUILDING)) waysByType.get(wayType.BUILDING).add(way);
                else if (way.isWayType(wayType.ROAD)) waysByType.get(wayType.ROAD).add(way);
                else if (way.isWayType(wayType.AMENITIES)) waysByType.get(wayType.AMENITIES).add(way);
                else if (way.isWayType(wayType.AEROWAYS)) waysByType.get(wayType.AEROWAYS).add(way);
                else if (way.isWayType(wayType.MAN_MADE)) waysByType.get(wayType.MAN_MADE).add(way);
                //            else {
                //                if(!(way.hasTag("highway", "proposed") && way.getTags().get("seamark:type") != null))
                //                    waysByType.get(wayType.OTHER).add(way);
                //            }
            }
        }


        // CREATE R TREES
        if (HCdata != null) {
           curves = new ArrayList<>(HCdata.getCurves());

            // hjemmelavet heightcurve til at indeholde alle andre
            // systemet går i stykker hvis OSMdata er null
            if (OSMdata != null) {
                List<Coordinate> coordlist = new ArrayList<>();
                coordlist.add(new Coordinate(OSMdata.getMinLat(), OSMdata.getMinLon()));
                coordlist.add(new Coordinate(OSMdata.getMinLat(), OSMdata.getMaxLon()));
                coordlist.add(new Coordinate(OSMdata.getMaxLat(), OSMdata.getMaxLon()));
                coordlist.add(new Coordinate(OSMdata.getMaxLat(), OSMdata.getMinLon()));
                coordlist.add(new Coordinate(OSMdata.getMinLat(), OSMdata.getMinLon()));
                outer = new HeightCurve(-1, 0, coordlist, sea.getChildren());
                curves.add(outer);
            }
            //

            curves.remove(sea);
            curves.removeIf(c -> c.getCoords() == null
                    || c.getCoords().isEmpty()
                    || c.getBoundingBox() == null);

            //HeightCurveRTree = new Rtree(new ArrayList<InsertableInRtree>(curves), 32);
        }

        if (OSMdata != null) {
            RtreesByType.put(wayType.COASTLINE,
                    new Rtree(new ArrayList<InsertableInRtree>(waysByType.get(wayType.COASTLINE)), 32));

        for (wayType type : wayType.values()) {
            if (type == wayType.COASTLINE) continue;
            List<OsmWay> list = waysByType.get(type);
            if (list != null && !list.isEmpty())
                RtreesByType.put(type,
                        new Rtree(new ArrayList<InsertableInRtree>(list), 32));
        }
    }


        //---------GUI CODE---------//
        //Creating SeaSlider and ScaleBar with our bottomControls
        bottomControls = new BottomControls(((observable, oldValue, newValue) -> {
            SEA_LEVEL = newValue.floatValue();
            updateSubmergedVertices();
            if (interactionHandler != null) {
                interactionHandler.recalculateRoute();
            }
            draw();
        }));

        upperControls = new UpperControls();

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
        updateSubmergedVertices();

        interactionHandler = new MapHandler(
                superAffine,
                this::draw,
                graph,
                () -> submergedVertices,
                () -> upperControls.getVehicleMenu().getVehicleType(),
                OSMdata,
                (route, totalHours) -> {
                    currentRoute = route != null ? route : new ArrayList<>();
                    if (route == null) {
                        upperControls.setRouteStatus("No route found for the selected points");
                    } else {
                        upperControls.setRouteStatus(
                                "Estimated time to get from start to target is " +
                                        formatEstimatedTime(totalHours) + " hr/min"
                        );
                    }
                    draw();
                }
        );
        upperControls.getVehicleMenu().setOnVehicleTypeChanged(interactionHandler::recalculateRoute);

        //BorderPane mouseEventComponent = new BorderPane();
        root.setOnMousePressed(interactionHandler::handleMousePressed);
        root.setOnMouseClicked(interactionHandler::handleMouseClicked);
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

    //TODO: CHANGE DEFAULT DATA TO RUN BINARY
    private void loadDefaultData() {
        HCdata = loadDefaultHeightCurveData();
        OSMdata = loadDefaultOsmData();
    }

    private void updateSubmergedVertices() {
        if (graph == null || OSMdata == null || HCdata == null || SEA_LEVEL <= 0.0F) {
            submergedVertices = new HashSet<>();
            return;
        }

        submergedVertices = SubmergedVertexFinder.findSubmergedVertices(graph, OSMdata, HCdata, SEA_LEVEL);
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
        AffineTransform combined = new AffineTransform(superAffine);
        combined.concatenate(t);
        //gc.setTransform(combined);

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
        //System.out.println("Manageable zoom level: " + zoom);

        float strokeWidth = (float) (1.0 / Math.sqrt(Math.abs(
                superAffine.getScaleX() * superAffine.getScaleY()
        )));
        strokeWidth = Math.clamp(strokeWidth, 0.5f, 1.0f);

        if (OSMdata != null) {
            //TODO: ADD FERRYWAYS, AEROWAYS AND AMENITIES, ASWELL AS MULTIPOLYGON FORREST
            //Draws waterways, lakes, ponds and so on
            drawLayer(wayType.WATER, gc, strokeWidth, zoom, combined);
            //Draws ferry routes and other sea route
            drawLayer(wayType.SEAROUTE, gc, strokeWidth, zoom, combined);
            //Draws polygon for coastline
            drawLayer(wayType.COASTLINE, gc, strokeWidth, zoom, combined);
            //Draws natural ways
            drawLayer(wayType.NATURAL, gc, strokeWidth, zoom, combined);
            //Draws landuse ways
            drawLayer(wayType.LANDUSE, gc, strokeWidth, zoom, combined);
            //Draws roads
            drawLayer(wayType.ROAD, gc, strokeWidth, zoom, combined);
            //Draws buildings
            drawLayer(wayType.BUILDING, gc, strokeWidth, zoom, combined);

            //TODO: LEFTEROVERS
            //Draws leisure ways, needs some fixing
            //drawLayer(wayType.LEISURE, gc, strokeWidth, zoom);
            //Draws otherwise non categorized ways
            //drawLayer(wayType.OTHER, gc, strokeWidth, zoom);
        }



        //HeightCurves raw paths
        if(HCdata != null) {
            // bruger outer som sea da dens children er identiske med sea
            outer.resetSubmerged();
            outer.submerge(SEA_LEVEL);

            float[] bbox = getViewPortBoundingBox();

            //List<InsertableInRtree> curves;
            //if (zoom > CutOffToRtree) {
            //curves = HeightCurveRTree.search(getViewPortBoundingBox());
            //curves = new ArrayList<>(HCdata.curves);
            for (HeightCurve curve : curves) {
                //HeightCurve curve = (HeightCurve) element;
                if (curve == sea) {
                    continue;
                }
                if (!curve.intersects(bbox)) continue;
                if (OSMdata != null) gc.setColor(curve.getFillColor(SEA_LEVEL, false));
                else gc.setColor(curve.getFillColor(SEA_LEVEL, true));
                if (curve.getCachedRegionPath() == null)
                    gc.fill(project(curve.getRegionPath(), combined));
                else
                    gc.fill(project(curve.getCachedRegionPath(), combined));
            }
        }

        drawCurrentRoute(gc, strokeWidth, combined);

        gc.dispose();
        render();
    }

    private void drawHeightCurvesRecursive(List<HeightCurve> curves, Graphics2D gc, Data projectionSource) {
        for (HeightCurve curve : curves) {
            if(OSMdata != null) gc.setColor(curve.getFillColor(SEA_LEVEL, false));
            else gc.setColor(curve.getFillColor(SEA_LEVEL, true));
            //gc.fill(project(curve.getRegionPath(), projectionSource));
            drawHeightCurvesRecursive(curve.getChildren(), gc, projectionSource);
        }
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
        if (way.isWayType(wayType.NATURAL)) key = "natural";
        else if (way.isWayType(wayType.ROAD)) key = "highway";
        else if (way.isWayType(wayType.LANDUSE)) key = "landuse";
        else if (way.isWayType(wayType.SEAROUTE)) key = "searoute";
        else if (way.isWayType(wayType.WATER)) key = "water";
        else if (way.isWayType(wayType.AMENITIES)) key = "amenity";
        else if (way.isWayType(wayType.AEROWAYS)) key = "aeroway";
        else if (way.isWayType(wayType.MAN_MADE)) key = "man_made";
//        else if (way.isLeisure()) {
//            key = "leisure";
//            return (float) jRoot.path(key).path("zoom").asDouble();
//        }
        else if (way.isWayType(wayType.BUILDING)) {
            key = "building";
            return (float) jRoot.path(key).path("zoom").asDouble();
        }
        else return 0;
        JsonNode jPath = jRoot.path(key).path(way.getValue()).path("zoom");

        if (jPath.isMissingNode()) return 0;

        return (float) jPath.asDouble();
    }

    private void drawLayer(wayType type, Graphics2D gc, float sw, float zoom, AffineTransform combined) {
        List<InsertableInRtree> ways;
        float[] bbox = getViewPortBoundingBox();

        if (zoom>CutOffToRtree) {
            Rtree rtree = RtreesByType.get(type);
            if (rtree == null) return;
            ways = rtree.search(bbox);
        }
        else {
            List<OsmWay> list = waysByType.get(type);
            if (list == null || list.isEmpty()) return;
            ways = new ArrayList<InsertableInRtree>(list);
        }
        //int x = 0;
        for (InsertableInRtree element : ways) {
            //x++;
            OsmWay way = (OsmWay) element;

            if (!way.intersects(bbox)) continue;

            if (type != wayType.WATER
                    && type != wayType.COASTLINE
                    && type != wayType.SEAROUTE
                    && zoom < getZoomLevel(way)) continue;

            Shape projected;
            //Shape s;
            if (way.getCachedShape() == null)
                projected = project(renderer.getWayPath(way), combined);
                //s = renderer.getWayPath(way);
            else
                projected = project(way.getCachedShape(), combined);
                //s = way.getCachedShape();
            renderer.setWayColor(way, sw);

            if (way.getNodes().getFirst().getId() == way.getNodes().getLast().getId()
                    && !(way.isWayType(wayType.ROAD) && !way.hasTag("highway", "rest_area")) && !way.isWayType(wayType.SEAROUTE))
                gc.fill(projected);
            else gc.draw(projected);
        }
        //System.out.println(x + " elements iterated in layer");
    }

    private void drawCurrentRoute(Graphics2D gc, float strokeWidth, AffineTransform combined) {
        if (currentRoute == null || currentRoute.isEmpty() || graph == null) {
            return;
        }

        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gc.setColor(ROUTE_COLOR);
        gc.setStroke(new BasicStroke(Math.max(3.0f, strokeWidth * 3.0f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (DirectedEdge edge : currentRoute) {
            Long fromId = graph.nodeIdOf(edge.from());
            Long toId = graph.nodeIdOf(edge.to());
            if (fromId == null || toId == null) {
                continue;
            }

            OsmNode from = OSMdata.getOsmNodeMap().get(fromId);
            OsmNode to = OSMdata.getOsmNodeMap().get(toId);
            if (from == null || to == null) {
                continue;
            }

            Shape projected = project(new Line2D.Float(from.getLon(), from.getLat(), to.getLon(), to.getLat()), combined);
            gc.draw(projected);
        }
    }

    private String formatEstimatedTime(float totalHours) {
        int totalMinutes = Math.round(totalHours * 60);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
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

    private static Shape project(Shape s, AffineTransform combined) {
        return combined.createTransformedShape(s);
    }

    private static float getZoom(Data d) {
        float p = 20, c = (float) Math.cos(Math.toRadians((d.getMinLat() + d.getMaxLat()) / 2));
        float w = (d.getMaxLon() - d.getMinLon()) * c, h = d.getMaxLat() - d.getMinLat();
        float k = Math.min((WIDTH - 2 * p) / w, (HEIGHT - 2 * p) / h);
        float superAffineZoom = (float) Math.sqrt(superAffine.getDeterminant());
        return k * superAffineZoom;
    }

    private static Point2D reverseProject(float sx, float sy, AffineTransform t){
            AffineTransform combined = new AffineTransform(superAffine);
            combined.concatenate(t);

            // taget fra chatGPT
            try {
                AffineTransform inverse = combined.createInverse();
                Point2D screenPt = new Point2D.Double(sx,sy);
                Point2D worldPt = new Point2D.Double();
                inverse.transform(screenPt, worldPt);

                return worldPt; // (lon, lat)
            } catch (NoninvertibleTransformException e) {
                throw new RuntimeException("Transform not invertible", e);
            }
        }
    public float getSEALEVEL() {return SEA_LEVEL;}

    private float[] getViewPortBoundingBox() {
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
