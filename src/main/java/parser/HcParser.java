package parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import models.Coordinate;
import models.HeightCurve;
import models.HeightCurveData;

public class HcParser {

    private final InputStream inputStream;

    public HcParser(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public HeightCurveData parse() {
        try {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + inputStream);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line;
            List<HeightCurve> curves = new ArrayList<>();
            List<HeightCurve> children = new ArrayList<>();
            List<Coordinate> coords = new ArrayList<>();

            long id = 0;
            float height = 0;
            float minLat = Float.MAX_VALUE;
            float minLon = Float.MAX_VALUE;
            float maxLat = Float.MIN_VALUE;
            float maxLon = Float.MIN_VALUE;

            while ((line = br.readLine()) != null) {

                if (line.contains("<bounds")) {
                    minLat = Float.parseFloat(getAttribute(line, "minlat"));
                    minLon = Float.parseFloat(getAttribute(line, "minlon"));
                    maxLat = Float.parseFloat(getAttribute(line, "maxlat"));
                    maxLon = Float.parseFloat(getAttribute(line, "maxlon"));
                }
                if (line.contains("<hc ")) {
                    id = Long.parseLong(getAttribute(line, "id"));
                    height = Float.parseFloat(getAttribute(line, "height"));
                    coords = new ArrayList<>();

                } else if (line.contains("<coords ")) {
                        float lat = Float.parseFloat(getAttribute(line, "lat"));
                        float lon = Float.parseFloat(getAttribute(line, "lon"));
                        coords.add(new Coordinate(lat, lon));

                        if (id != -1) {
                            minLat = Math.min(minLat, lat);
                            minLon = Math.min(minLon, lon);
                            maxLat = Math.max(maxLat, lat);
                            maxLon = Math.max(maxLon, lon);
                        }
                } else if (line.contains("</hc>")) {// ← closing angle bracket added here
                    if (id != -1) {
                        curves.add(new HeightCurve(id, height, coords, new ArrayList<>()));
                    }
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
                    sea.getChildren().add(child);
                } else {
                    parent.getChildren().add(child);
                }
            }
            return new HeightCurveData(minLat, minLon, maxLat, maxLon, sea, curves);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    //Contains
    private boolean contains(HeightCurve outer, HeightCurve inner) {

        if(outer.getCoords().isEmpty() || inner.getCoords().isEmpty()) return false;

        Coordinate p = inner.getCoords().get(0);
        return outer.getBoundaryPath().contains(p.getLon(), p.getLat());
    }

    //Area
    private float area(HeightCurve c) {
        List<Coordinate> c_coords = c.getCoords();

        float sum = 0;

        for(int i = 0; i < c_coords.size(); i++) {
            Coordinate a = c_coords.get(i);
            Coordinate b = c_coords.get((i+1) % c.getCoords().size());

            sum += a.getLon() * b.getLat() - a.getLat() * b.getLon();
        }

        return (float) (Math.abs(sum) / 2.0);
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
