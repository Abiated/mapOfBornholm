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
import parser.Parser;
import rendering.*;
import spatialDatastructures.Rtree;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class App extends DrawingApp {

    //AppState and GUI data
    private static final int WIDTH = getWIDTH();
    private static final int HEIGHT = getHEIGHT();
    private static final Color WATER_COLOR = Color.decode("#2b8cbe");
    private static final Color ROUTE_COLOR = Color.decode("#d7301f");
    private BottomControls bottomControls;
    private UpperControls upperControls;
    private Float SEA_LEVEL = 0.0F;
    private JsonNode jRoot;

    //MapData
    private HeightCurveData HCdata;
    private Parser OsmData;

    //Data
    private HeightCurve sea;
    private HeightCurve outer;
    private HashMap<Long, OsmWay> ways;
    private List<HeightCurve> curves;

    //Datastructures
    private final EnumMap<wayType, List<OsmElement>> waysByType = new EnumMap<>(wayType.class);
    private final EnumMap<wayType,Rtree> RtreesByType = new EnumMap<>(wayType.class);
    private final int CutOffToRtree = 7;

    //Pathfinding data
    private EdgeWeightedDigraph graph;
    private MapHandler interactionHandler;
    private List<DirectedEdge> currentRoute = new ArrayList<>();
    private Set<Integer> submergedVertices = new HashSet<>();
    private List<OsmRelation> multipolygon;

    //Rendering data
    private MapProjection projection;
    private static final SuperAffine superAffine = new SuperAffine();
    private Data projectionSource;
    //private AffineTransform t;
    private Renderer renderer;


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

        DataLoader loader = new DataLoader();
        DataLoader.MapData mapData = (chosen == null)
                ? loader.loadDefaultData() : loader.load(chosen);

        OsmData = mapData.OsmData();
        HCdata = mapData.HcData();

        //Get sea from HCdata and ways
        if (HCdata != null) sea = HCdata.sea;
        if (OsmData != null) ways = OsmData.getOsmWayMap();
        if (OsmData != null) projectionSource = OsmData;
        else projectionSource = HCdata;
        //t = MapProjection.getProjectionTransform(projectionSource);

        projection = new MapProjection(superAffine, projectionSource);

        //---------CONFIG PARSING---------//
        //For now default json file maybe later there can be a choice.
        try {
            InputStream is = getClass().getResourceAsStream("/config.json");
            if (is == null) System.out.println("File not found");

            jRoot = (new ObjectMapper()).readTree(is);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        if (OsmData != null) graph = EdgeWeightedDigraph.constructGraph(OsmData, jRoot);

        if(OsmData != null)
            multipolygon = OsmData.getOsmRelationMap().values().stream().filter(r -> r.getTags() != null
            && r.hasTag("type", "multipolygon")).toList();

        if (OsmData != null) {
            for (wayType type : wayType.values()) {
                waysByType.put(type, new ArrayList<>());
            }

            insertIntoWaysByType(ways.values());
            insertIntoWaysByType(multipolygon);

        }


        // CREATE R TREES
        if (HCdata != null) {
           curves = new ArrayList<>(HCdata.getCurves());

            // hjemmelavet heightcurve til at indeholde alle andre
            // systemet går i stykker hvis OsmData er null
            if (OsmData != null) {
                List<Coordinate> coordlist = new ArrayList<>();
                coordlist.add(new Coordinate(OsmData.getMinLat(), OsmData.getMinLon()));
                coordlist.add(new Coordinate(OsmData.getMinLat(), OsmData.getMaxLon()));
                coordlist.add(new Coordinate(OsmData.getMaxLat(), OsmData.getMaxLon()));
                coordlist.add(new Coordinate(OsmData.getMaxLat(), OsmData.getMinLon()));
                coordlist.add(new Coordinate(OsmData.getMinLat(), OsmData.getMinLon()));
                outer = new HeightCurve(-1, 0, coordlist, sea.getChildren());
                curves.add(outer);
            }


            curves.remove(sea);
            curves.removeIf(c -> c.getCoords() == null
                    || c.getCoords().isEmpty()
                    || c.getBoundingBox() == null);

            //HeightCurveRTree = new Rtree(new ArrayList<InsertableInRtree>(curves), 32);
        }

        if (OsmData != null) {
            RtreesByType.put(wayType.COASTLINE,
                    new Rtree(new ArrayList<>(waysByType.get(wayType.COASTLINE)), 32));

        for (wayType type : wayType.values()) {
            if (type == wayType.COASTLINE) continue;
            List<OsmElement> list = waysByType.get(type);
            if (list != null && !list.isEmpty())
                RtreesByType.put(type,
                        new Rtree(new ArrayList<>(list), 32));
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
                projection,
                route -> {
                    currentRoute = route != null ? route : new ArrayList<>();
                    if (route == null) {
                        upperControls.setRouteStatus("No route found for the selected points");
                    } else {
                        upperControls.setRouteStatus(
                                "Estimated time to get from start to target is " +
                                        formatEstimatedTime(route, upperControls.getVehicleMenu().getVehicleType()) + " hr/min"
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

    private void insertIntoWaysByType(Collection<? extends OsmElement> elements) {
        for(OsmElement element : elements ) {
            if (element.getType(wayType.COASTLINE)) {
                if (element instanceof OsmWay) {
                    //Handles smaller coastlines
                    if (((OsmWay) element).isClosed())     waysByType.get(wayType.COASTLINE).add(element);
                } else if (element instanceof OsmRelation) {
                    if (element.getType(wayType.COASTLINE)) waysByType.get(wayType.COASTLINE).add(element);
                }
            }
            else if (element.getType(wayType.WATER))        waysByType.get(wayType.WATER).add(element);
            else if (element.getType(wayType.SEAROUTE))     waysByType.get(wayType.SEAROUTE).add(element);
            else if (element.getType(wayType.NATURAL))      waysByType.get(wayType.NATURAL).add(element);
            else if (element.getType(wayType.LANDUSE))      waysByType.get(wayType.LANDUSE).add(element);
            else if (element.getType(wayType.BUILDING))     waysByType.get(wayType.BUILDING).add(element);
            else if (element.getType(wayType.ROAD))         waysByType.get(wayType.ROAD).add(element);
            else if (element.getType(wayType.AMENITIES))    waysByType.get(wayType.AMENITIES).add(element);
            else if (element.getType(wayType.AEROWAYS))     waysByType.get(wayType.AEROWAYS).add(element);
            else if (element.getType(wayType.MAN_MADE))     waysByType.get(wayType.MAN_MADE).add(element);
        }
    }


    private void updateSubmergedVertices() {
        if (graph == null || OsmData == null || HCdata == null || SEA_LEVEL <= 0.0F) {
            submergedVertices = new HashSet<>();
            return;
        }

        submergedVertices = SubmergedVertexFinder.findSubmergedVertices2(graph, OsmData, HCdata, SEA_LEVEL);
    }

    //FOR DRAWING AND RENDERING
    public void draw() {

        Graphics2D gc = getNewGraphicsContext();
        renderer = new Renderer(gc, jRoot);
        AffineTransform combined = projection.getCombined();
        //gc.setTransform(combined);

        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //Background
        gc.setColor(WATER_COLOR);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        //Zoom
        //DATA CONVERTED TO LOCAL VARIABLE
        float zoom = projection.getZoom();
        bottomControls.update(zoom);

        //Midlertidig løsning måske?
        zoom = zoom / 1000; //Making the zoom level manageable for rendering with zoom
        //System.out.println("Manageable zoom level: " + zoom);

        float strokeWidth = (float) (1.0 / Math.sqrt(Math.abs(
                superAffine.getScaleX() * superAffine.getScaleY()
        )));
        strokeWidth = Math.clamp(strokeWidth, 0.5f, 1.0f);

        if (OsmData != null) {
            //Draws waterways, lakes, ponds and so on
            renderer.drawLayer(wayType.WATER, gc, strokeWidth, zoom, projection, CutOffToRtree, waysByType, RtreesByType);
            //Draws ferry routes and other sea route
            renderer.drawLayer(wayType.SEAROUTE, gc, strokeWidth, zoom, projection, CutOffToRtree, waysByType, RtreesByType);
            //Draws polygon for coastline
            renderer.drawLayer(wayType.COASTLINE, gc, strokeWidth, zoom, projection, CutOffToRtree, waysByType, RtreesByType);
            //Draw aeroways
            renderer.drawLayer(wayType.AEROWAYS, gc, strokeWidth, zoom, projection, CutOffToRtree, waysByType, RtreesByType);
            //Draws landuse ways
            renderer.drawLayer(wayType.LANDUSE, gc, strokeWidth, zoom, projection, CutOffToRtree, waysByType, RtreesByType);
            //Draws natural ways
            renderer.drawLayer(wayType.NATURAL, gc, strokeWidth, zoom, projection, CutOffToRtree, waysByType, RtreesByType);
            //Draws man_made
            renderer.drawLayer(wayType.MAN_MADE, gc, strokeWidth, zoom, projection, CutOffToRtree, waysByType, RtreesByType);
            //Draws amenity
            renderer.drawLayer(wayType.AMENITIES, gc, strokeWidth, zoom, projection, CutOffToRtree, waysByType, RtreesByType);
            //Draws roads
            renderer.drawLayer(wayType.ROAD, gc, strokeWidth, zoom, projection, CutOffToRtree, waysByType, RtreesByType);
            //Draws buildings
            renderer.drawLayer(wayType.BUILDING, gc, strokeWidth, zoom, projection, CutOffToRtree, waysByType, RtreesByType);
        }

        //HeightCurves raw paths
        if(HCdata != null) {
            // bruger outer som sea da dens children er identiske med sea
            if(outer != null) {
                outer.resetSubmerged();
                outer.submerge(SEA_LEVEL);
            } else {
                sea.resetSubmerged();
                sea.submerge(SEA_LEVEL);
           }

            float[] bbox = projection.getViewPortBoundingBox();

            //List<InsertableInRtree> curves;
            //if (zoom > CutOffToRtree) {
            //curves = HeightCurveRTree.search(getViewPortBoundingBox());
            //curves = new ArrayList<>(HCdata.curves);
            for (HeightCurve curve : curves) {
                if (curve == sea) continue;
                if (!curve.intersects(bbox)) continue;

                gc.setColor(curve.getFillColor(SEA_LEVEL, OsmData == null));

                Shape path = curve.getCachedRegionPath() != null
                        ? curve.getCachedRegionPath()
                        : curve.getRegionPath();
                Shape projected = project(path, combined);

                Coordinate first = curve.getCoords().getFirst();
                Coordinate last = curve.getCoords().getLast();
                boolean isClosed = first.getLon() == last.getLon() && first.getLat() == last.getLat();

                if(isClosed) gc.fill(projected);
                else gc.draw(projected);
            }
        }

        drawCurrentRoute(gc, strokeWidth, combined);

        gc.dispose();
        render();
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

            OsmNode from = OsmData.getOsmNodeMap().get(fromId);
            OsmNode to = OsmData.getOsmNodeMap().get(toId);
            if (from == null || to == null) {
                continue;
            }

            Shape projected = project(new Line2D.Float(from.getLon(), from.getLat(), to.getLon(), to.getLat()), combined);
            gc.draw(projected);
        }
    }

    private String formatEstimatedTime(List<DirectedEdge> route, PathFinder.VehicleType vehicleType) {
        float totalHours = 0.0f;
        for (DirectedEdge edge : route) {
            totalHours += edge.weight(vehicleType);
        }

        int totalMinutes = Math.round(totalHours * 60);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    private static Shape project(Shape s, AffineTransform combined) {
        return combined.createTransformedShape(s);
    }
}

