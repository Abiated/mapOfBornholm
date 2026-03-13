//package dk.itu.parser;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//
//public class HcParser {
//
//    private final String fileName;
//
//    public HcParser(String fileName) {
//        this.fileName = fileName;
//    }
//
//    public HeightCurveData parse() {
//        try {
//            InputStream is = HcParser.class.getResourceAsStream("/" + fileName);
//            if (is == null) {
//                throw new IllegalArgumentException("Resource not found: " + fileName);
//            }
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
//
//            String line;
//            List<HeightCurve> curves = new ArrayList<>();
//            List<HeightCurve> children = new ArrayList<>();
//            List<Coordinate> coords = new ArrayList<>();
//
//            long id = 0;
//            double height = 0;
//            double minLat = 0;
//            double minLon = 0;
//            double maxLat = 0;
//            double maxLon = 0;
//            int count = 0;
//
//
//            while ((line = br.readLine()) != null) {
//                // TODO: Implement parsing
//
//                if(line.contains("<bounds")) {
//                    minLat = Double.parseDouble(getAttribute(line, "minlat"));
//                    minLon = Double.parseDouble(getAttribute(line, "minlon"));
//                    maxLat = Double.parseDouble(getAttribute(line, "maxlat"));
//                    maxLon = Double.parseDouble(getAttribute(line, "maxlon"));
//
//                }
//                if (line.contains("<hc")) {
//                    id = Long.parseLong(getAttribute(line, "id"));
//                    height = Double.parseDouble(getAttribute(line, "height"));
//                    coords = new ArrayList<>();
//
//                } else if (line.contains("<coord")) {
//                    double lat = Double.parseDouble(getAttribute(line, "lat"));
//                    double lon = Double.parseDouble(getAttribute(line, "lon"));
//                    coords.add(new Coordinate(lat, lon));
//
//                } else if(line.contains("</hc")) {
//                    HeightCurve curve = new HeightCurve(id, height, coords, new ArrayList<>());
//                    curves.add(curve);
//
//                }
//
//            }
//
//            HeightCurve sea = new HeightCurve(0, -1, new ArrayList<>(), new ArrayList<>());
//            curves.add(0,sea);
//
//            for(HeightCurve child  : curves) {
//
//                if(child == sea) continue;
//
//                HeightCurve parent = null;
//
//                for(HeightCurve candi : curves) {
//
//                    if(candi == child ||candi == sea) continue;
//
//                    if(contains(candi, child)) {
//
//                        if(parent == null) {
//                            parent = candi;
//                        } else if(area(candi) < area(parent)) {
//                            parent = candi;
//                        }
//                    }
//                }
//
//                if(parent == null) {
//                    sea.children.add(child);
//                } else {
//                    parent.children.add(child);
//                }
//            }
//            return new HeightCurveData(minLat, minLon, maxLat, maxLon, sea, curves);
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        }
//
//        // TODO: Return the parsed HeightCurveData object.
//        return null;
//    }
//
//    //Contains
//    private boolean contains(HeightCurve outer, HeightCurve inner) {
//
//        if(outer.coords.isEmpty() || inner.coords.isEmpty()) return false;
//
//        Coordinate p = inner.coords.get(0);
//        return outer.getBoundaryPath().contains(p.getLon(), p.getLat());
//    }
//
//    //Area
//    private double area(HeightCurve c) {
//        List<Coordinate> c_coords = c.coords;
//
//        double sum = 0;
//
//        for(int i = 0; i < c_coords.size(); i++) {
//            Coordinate a = c_coords.get(i);
//            Coordinate b = c_coords.get((i+1) % c.coords.size());
//
//            sum += a.getLon() * b.getLat() - a.getLat() * b.getLon();
//        }
//
//        return Math.abs(sum) / 2.0;
//    }
//
//    //getAtrribute
//    public String getAttribute(String s, String key) {
//        String pattern = key + "=\"";
//        int start = s.indexOf(pattern);
//        if(start == -1) {
//            return null;
//        }
//        int startValue = start + pattern.length();
//        int endValue = s.indexOf('"',startValue);
//        return s.substring(startValue, endValue);
//    }
//}
