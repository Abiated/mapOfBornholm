package interfaces;

public interface InsertableInRtree {

    float[] getBoundingBox();
    boolean intersects(float[] otherBbox);

}
