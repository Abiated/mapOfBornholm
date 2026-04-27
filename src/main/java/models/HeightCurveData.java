package models;

import interfaces.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Parsed height curve data.
 *
 * Intended as the output of the (optional) .hc parser as well as the hard-coded
 * examples
 */
public class HeightCurveData implements Data, Serializable {
    private static final long serialVersionUID = 1L;

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

    //Exist for deserializable as it needs a no argument constructor, value does not matter
    private HeightCurveData() {
        this.minLat = 0;
        this.minLon = 0;
        this.maxLat = 0;
        this.maxLon = 0;
        this.sea = null;
        this.curves = new ArrayList<>();
    }

    @Override
    public float getMinLat() {return minLat;}

    @Override
    public float getMaxLat() {return maxLat;}

    @Override
    public float getMinLon() {return minLon;}

    @Override
    public float getMaxLon() {return  maxLon;}

    public List<HeightCurve> getCurves() {
        return curves;
    }

}