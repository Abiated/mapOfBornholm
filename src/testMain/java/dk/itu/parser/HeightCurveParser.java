package dk.itu.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HeightCurveParser {

    private final String fileName;

    public HeightCurveParser(String fileName) {
        this.fileName = fileName;
    }

    public HeightCurveData parse() {
        try {
            InputStream is = HeightCurveParser.class.getResourceAsStream("/data/" + fileName);
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + fileName);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            String line;
            List<HeightCurve> curves = new ArrayList<>();
            List<HeightCurve> children = new ArrayList<>();
            List<Coordinate> coords = new ArrayList<>();

            long id = 0;
            float height = 0;
            float minLat = 0;
            float minLon = 0;
            float maxLat = 0;
            float maxLon = 0;
            int count = 0;


            while ((line = br.readLine()) != null) {
                // TODO: Implement parsing

                if(line.contains("<bounds")) {
                    minLat = Float.parseFloat(getAttribute(line, "minlat"));
                    minLon = Float.parseFloat(getAttribute(line, "minlon"));
                    maxLat = Float.parseFloat(getAttribute(line, "maxlat"));
                    maxLon = Float.parseFloat(getAttribute(line, "maxlon"));

                }
                if (line.contains("<hc")) {
                    if (getAttribute(line, "id")!=null)
                        id = Long.parseLong(getAttribute(line, "id"));
                    if (getAttribute(line, "height") != null)
                        height = Float.parseFloat(getAttribute(line, "height"));
                    coords = new ArrayList<>();

                } else if (line.contains("<coord")) {
                    float lat = Float.parseFloat(getAttribute(line, "lat"));
                    float lon = Float.parseFloat(getAttribute(line, "lon"));
                    coords.add(new Coordinate(lat, lon));

                } else if(line.contains("</hc")) {
                    if (id != -1) {
                        curves.add(new HeightCurve(id, height, coords, new ArrayList<>()));
                    }
//                    HeightCurve curve = new HeightCurve(id, height, coords, new ArrayList<>());
//                    curves.add(curve);

                }

            }

            HeightCurve sea = new HeightCurve(0, -1, new ArrayList<>(), new ArrayList<>());
            curves.add(0,sea);

            for(HeightCurve child  : curves) {

                if(child == sea) continue;

                HeightCurve parent = null;

                for(HeightCurve candi : curves) {

                    if(candi == child ||candi == sea) continue;

                    if(contains(candi, child)) {

                        if(parent == null) {
                            parent = candi;
                        } else if(area(candi) < area(parent)) {
                            parent = candi;
                        }
                    }
                }

                if(parent == null) {
                    sea.children.add(child);
                } else {
                    parent.children.add(child);
                }
            }
            return new HeightCurveData(minLat, minLon, maxLat, maxLon, sea, curves);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        // TODO: Return the parsed HeightCurveData object.
        return null;
    }

    //Contains
    private boolean contains(HeightCurve outer, HeightCurve inner) {

        if(outer.coords.isEmpty() || inner.coords.isEmpty()) return false;

        Coordinate p = inner.coords.get(0);
        return outer.getBoundaryPath().contains(p.getLon(), p.getLat());
    }

    //Area
    private float area(HeightCurve c) {
        List<Coordinate> c_coords = c.coords;

        float sum = 0;

        for(int i = 0; i < c_coords.size(); i++) {
            Coordinate a = c_coords.get(i);
            Coordinate b = c_coords.get((i+1) % c.coords.size());

            sum += (float) (a.getLon() * b.getLat() - a.getLat() * b.getLon());
        }

        return Math.abs(sum) / 2.0F;
    }

    //getAtrribute
    public String getAttribute(String s, String key) {
        String pattern = key + "=\"";
        int start = s.indexOf(pattern);
        if(start == -1) {
            return null;
        }
        int startValue = start + pattern.length();
        int endValue = s.indexOf('"',startValue);
        return s.substring(startValue, endValue);
    }
}
