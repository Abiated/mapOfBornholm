package interfaces;

public interface InsertableInRtree {

    public float[] getBoundingBox();

    public boolean intersects(float[] otherBbox);

}
