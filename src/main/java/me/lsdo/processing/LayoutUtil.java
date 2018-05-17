package me.lsdo.processing;

import java.util.*;

// Generally useful vector math and conversion functions

public class LayoutUtil {

    // Create a new vector (x, y)
    static public PVector2 V(double x, double y) {
        return new PVector2((float)x, (float)y);
    }

    // Clone a vector
    public static PVector2 V(PVector2 v) {
        return V(v.x, v.y);
    }

    // Return a + b
    static public PVector2 Vadd(PVector2 a, PVector2 b) {
        return PVector2.add(a, b);
    }

    // Return a - b
    public static PVector2 Vsub(PVector2 a, PVector2 b) {
        return Vadd(a, Vmult(b, -1.));
    }

    // Return k * a
    public static PVector2 Vmult(PVector2 v, double k) {
        return PVector2.mult(v, (float)k);
    }

    // Return v rotated counter-clockwise by theta radians
    public static PVector2 Vrot(PVector2 v, double theta) {
        PVector2 rot = V(v);
        rot.rotate((float)theta);
        return rot;
    }

    // Compute a basis transformation for vector p, where u is the transformation result of basis vector U (1, 0),
    // and v is the transformation of basis V (0, 1)
    public static PVector2 basisTransform(PVector2 p, PVector2 U, PVector2 V) {
        return Vadd(Vmult(U, p.x), Vmult(V, p.y));
    }

    public static interface Transform {
        public PVector2 transform(PVector2 p);
    }

    // Convert a set of points in bulk according to some transformation function.
    public static ArrayList<PVector2> transform(ArrayList<PVector2> points, Transform tx) {
        ArrayList<PVector2> transformed = new ArrayList<PVector2>();
        for (PVector2 p : points) {
            transformed.add(tx.transform(p));
        }
        return transformed;
    }

    // Transformation that translates a point by 'offset'
    public static Transform translate(final PVector2 offset) {
        return new Transform() {
            public PVector2 transform(PVector2 p) {
                return Vadd(p, offset);
            }
        };
    }

    // Convert (x, y) coordinate p to screen pixel coordinates where top-left is pixel (0, 0) and bottom-right is
    // pixel (width, height). 'span' is the size of the viewport in world coordinates, where size means width if horizSpan is
    // true and height if horizSpan is false. World origin is in the center of the viewport.
    public static PVector2 xyToScreen(PVector2 p, int width, int height, double span, boolean horizSpan) {
        double scale = span / (horizSpan ? width : height);
        PVector2 U = V(1. / scale, 0);
        PVector2 V = V(0, -1. / scale);
        PVector2 offset = Vmult(V(width, height), .5);
        return Vadd(basisTransform(p, U, V), offset);
    }

    public static PVector2 xyToScreen(PVector2 p, int width, int height) {
	return xyToScreen(p, width, height, 2., false);
    }

    // Inverse of xyToScreen
    public static PVector2 screenToXy(PVector2 p, int width, int height, double span, boolean horizSpan) {
        double scale = span / (horizSpan ? width : height);
        PVector2 U = V(scale, 0);
        PVector2 V = V(0, -scale);
        PVector2 offset = Vmult(V(width, height), .5);
        return basisTransform(Vsub(p, offset), U, V);
    }

    // Convert (x, y) coordinate to polar coordinates (radius, theta [counter-clockwise])
    public static PVector2 xyToPolar(PVector2 p) {
        return V(p.mag(), Math.atan2(p.y, p.x));
    }

    // Convert polar coordinates (radius, theta [counter-clockwise]) to cartesian (x, y)
    public static PVector2 polarToXy(PVector2 p) {
        double r = p.x;
        double theta = p.y;
        return Vrot(V(r, 0), theta);
    }

}
