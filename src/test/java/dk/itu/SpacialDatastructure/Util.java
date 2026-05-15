package dk.itu.SpacialDatastructure;

import interfaces.InsertableInRtree;
import models.OsmNode;

public class Util {

    private static long idCounter = 1;

    public static class TestNode implements InsertableInRtree {

        private final OsmNode node;

        public TestNode(float lat, float lon) {
            this.node = new OsmNode(idCounter++, lat, lon);
        }

        public OsmNode getNode() {
            return node;
        }

        @Override
        public float[] getBoundingBox() {
            float lat = node.getLat();
            float lon = node.getLon();
            return new float[]{lon, lon, lat, lat};
        }

        @Override
        public boolean intersects(float[] bbox) {
            float lat = node.getLat();
            float lon = node.getLon();

            return !(lon < bbox[0] || lon > bbox[1] ||
                    lat < bbox[2] || lat > bbox[3]);
        }
    }

    // single node
    public static InsertableInRtree node(float lat, float lon) {
        return new TestNode(lat, lon);
    }

    // multiple nodes
    public static java.util.List<InsertableInRtree> nodes(float[][] coords) {
        java.util.List<InsertableInRtree> list = new java.util.ArrayList<>();

        for (float[] c : coords) {
            list.add(node(c[0], c[1]));
        }

        return list;
    }

    // grid notes
    public static java.util.List<InsertableInRtree> grid(int rows, int cols, float spacing) {
        java.util.List<InsertableInRtree> list = new java.util.ArrayList<>();

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                list.add(node(x * spacing, y * spacing));
            }
        }

        return list;
    }

    // lines of notes
    public static java.util.List<InsertableInRtree> line(int count, float startX, float startY, float dx, float dy) {
        java.util.List<InsertableInRtree> list = new java.util.ArrayList<>();

        for (int i = 0; i < count; i++) {
            list.add(node(
                    startX + i * dx,
                    startY + i * dy
            ));
        }

        return list;
    }

    /**
     * Reset IDs for deterministic tests
     */
    public static void resetIds() {
        idCounter = 1;
    }
}