package rendering;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import GUI.SuperAffine;
import interfaces.Data;

public class MapProjection {

    private static final int WIDTH = DrawingApp.getWIDTH();
    private static final int HEIGHT = DrawingApp.getHEIGHT();
    private final float p = 20f;

    private final SuperAffine superAffine;
    private final AffineTransform t;
    private final float k;
    private final float c;

    private AffineTransform combinedCache;
    private double[] lastSuperAffineMatrix = new double[6];


    public MapProjection(SuperAffine superAffine, Data data) {
        this.superAffine = superAffine;
        this.c = (float) Math.cos(Math.toRadians((data.getMinLat() + data.getMaxLat()) / 2));

        float w = (data.getMaxLon() - data.getMinLon()) * c, h = data.getMaxLat() - data.getMinLat();
        this.k = Math.min((WIDTH - 2 * p) / w, (HEIGHT - 2 * p) / h);

        AffineTransform t = new AffineTransform();
        t.translate(p + (WIDTH - 2 * p - w * k) / 2, p + (HEIGHT - 2 * p - h * k) / 2 + h * k);
        t.scale(k, -k);
        t.scale(c, 1);
        t.translate(-data.getMinLon(), -data.getMinLat());
        this.t = t;

    }

    public AffineTransform getCombined() {
        double[] current = new double[6];
        superAffine.getMatrix(current);

        if(combinedCache == null || !java.util.Arrays.equals(current, lastSuperAffineMatrix)) {
            combinedCache = new AffineTransform(superAffine);
            combinedCache.concatenate(t);
            lastSuperAffineMatrix = current;
        }

        return combinedCache;
    }

    public Shape project(Shape s) {
        return getCombined().createTransformedShape(s);
    }

    public Point2D screenToWorld(double screenX, double screenY) {
        try {
            Point2D world = new Point2D.Double();
            getCombined().createInverse().transform(new Point2D.Double(screenX, screenY), world);
            return world;
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }

    }

    public static AffineTransform getProjectionTransform(Data d) {
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

    public float getZoom() {
        float superAffineZoom = (float) Math.sqrt(superAffine.getDeterminant());
        return k * superAffineZoom;
    }

    public float[] getViewPortBoundingBox() {
        Point2D topleft = screenToWorld(0, 0);
        Point2D topright = screenToWorld(WIDTH,0);
        Point2D bottomright = screenToWorld(WIDTH,HEIGHT);
        Point2D bottomleft = screenToWorld(0,HEIGHT);

        float minX = (float) Math.min(bottomleft.getX(),Math.min(bottomright.getX(),Math.min(topleft.getX(),topright.getX())));
        float maxX = (float) Math.max(bottomleft.getX(),Math.max(bottomright.getX(),Math.max(topleft.getX(),topright.getX())));
        float minY = (float) Math.min(bottomleft.getY(),Math.min(bottomright.getY(),Math.min(topleft.getY(),topright.getY())));
        float maxY = (float) Math.max(bottomleft.getY(),Math.max(bottomright.getY(),Math.max(topleft.getY(),topright.getY())));

        return new float[] {minX,maxX,minY,maxY};
    }

    public AffineTransform getBaseTransform() {
        return new AffineTransform(t);
    }
}
