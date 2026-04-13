package rendering;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class SuperAffine extends AffineTransform {
    public SuperAffine() {
        super();
    }

    public SuperAffine reset() {
        setToIdentity();
        return this;
    }

    /**
     * Prepends a translation transformation to this transform.
     * @param x The x translation
     * @param y The y translation
     * @return This transform for chaining
     */
    public SuperAffine prependTranslation(float x, float y) {
        AffineTransform translate = AffineTransform.getTranslateInstance(x, y);
        translate.concatenate(this);
        setTransform(translate);
        return this;
    }

    /**
     * Prepends a scaling transformation to this transform.
     * @param x The x scale factor
     * @param y The y scale factor
     * @return This transform for chaining
     */
    public SuperAffine prependScale(float x, float y) {
        AffineTransform scale = AffineTransform.getScaleInstance(x, y);
        scale.concatenate(this);
        setTransform(scale);
        return this;
    }

    /**
     * Calculates the base stroke width used for when drawing
     * @return The scale-adjusted width for strokes when drawing
     */
    public float getStrokeBaseWidth() {
        return (float) (1/Math.sqrt(this.getDeterminant()));
    }

    /**
     * Transforms the specified point by the inverse of this transform.
     * @param x The X coordinate of the point to transform
     * @param y The Y coordinate of the point to transform
     * @return A Point2D containing the transformed point [x, y]. Returns [0,0] if it can't transform
     */
    public Point2D inverseTransform(float x, float y) {
        try {
            // Get the matrix elements
            float m00 = (float) getScaleX();
            float m10 = (float) getShearY();
            float m01 = (float) getShearX();
            float m11 = (float) getScaleY();
            float m02 = (float) getTranslateX();
            float m12 = (float) getTranslateY();

            // Calculate the determinant
            float det = m00 * m11 - m01 * m10;
            if (det == 0) {
                throw new NoninvertibleTransformException("Transform is not invertible");
            }

            // Calculate the inverse transform coordinates
            float invDet = (float) (1.0 / det);
            float transformedX = ((x - m02) * m11 - (y - m12) * m01) * invDet;
            float transformedY = ((y - m12) * m00 - (x - m02) * m10) * invDet;

            return new Point2D.Float(transformedX, transformedY);
        } catch (NoninvertibleTransformException e) {
            System.out.println("Could not inverse transform point: " + e.getMessage());
            return new Point2D.Float(0, 0);
        }
    }
}
