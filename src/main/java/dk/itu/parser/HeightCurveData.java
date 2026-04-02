package dk.itu.parser;

import java.util.List;

/**
 * Parsed height curve data.
 *
 * Intended as the output of the (optional) .hc parser as well as the hard-coded
 * examples
 */
public class HeightCurveData {

    public final float minLat;
    public final float minLon;
    public final float maxLat;
    public final float maxLon;

    /**
     * Implicit "sea" root of the containment tree. Its {@code children} are the
     * outermost coastlines.
     */
    public final HeightCurve sea;

    /**
     * Flat list of all curves (may include {@link #sea}).
     */
    public final List<HeightCurve> curves;

    public HeightCurveData(float minLat, float minLon, float maxLat, float maxLon, HeightCurve sea, List<HeightCurve> curves) {
        this.minLat = minLat;
        this.minLon = minLon;
        this.maxLat = maxLat;
        this.maxLon = maxLon;
        this.sea = sea;
        this.curves = List.copyOf(curves);
    }

    public List<HeightCurve> getCurves() {
        return curves;
    }
}

