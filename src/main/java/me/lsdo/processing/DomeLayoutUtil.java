package me.lsdo.processing;

import java.util.*;

enum PanelLayout {
    _2,
    _6,
    _13,
    _24
}

// Util functions specifically for laying out the dome panel geometry

public class DomeLayoutUtil {

    static PVector2 V(double x, double y) {
	return LayoutUtil.V(x, y);
    }
    
    /**
     * Axial coordinates are a coordinate system referring to vertices of a triangular grid. The
     * grid is oriented such that triangle edges are horizontal (coincident with the x-axis) and at
     * +/-60 degrees. For a grid coordinate (u, v), u refers to coordinates increasing from the
     * origin in the line 60 degrees counter-clockwise from the +x axis, while v refers to
     * increasing along the x-axis.
     */

    static double SQRT_3 = Math.sqrt(3.);
    static double PI = Math.PI;

    // Spacing between points of a triangular grid of size 'n' where distance between edge points on opposing
    // panels is 'k' times the distance between adjacent points on the same panel.
    public static double pixelSpacing(int n) {
        // Even spacing across the panel gap. .5*SQRT_3 may be a better choice as it makes the density more
        // consistent across the gap (the gaps jump out less), but makes a bunch of other logic more complicated.
        double k = 1.;
        return 1. / (n - 1 + k * SQRT_3);
    }

    // Evenly fill a triangle with a grid of points of size n. The triangle filled is an equilateral triangle
    // with points at (0, 0), (1, 0), and (.5, sqrt(3)/2). Returns a list of points traversed in a boustrophedon
    // manner, starting near the origin, proceeding left/right, then upward. The point near (0, 0) will thus be
    // known as the 'entry' point, and the top-most point as the 'exit' point.
    public static ArrayList<TriCoord> fillTriangle(int n) {
        ArrayList<TriCoord> coords = new ArrayList<TriCoord>();
        for (int row = 0; row < n; row++) {
            boolean reversed = (row % 2 == 1);
            int width = n - row;
            for (int col = 0; col < width; col++) {
                int c = (reversed ? width - 1 - col : col);
                TriCoord tc = TriCoord.fromParts(TriCoord.CoordType.PIXEL,
                  TriCoord.Axis.U, row, TriCoord.Axis.V, c, TriCoord.PanelOrientation.A, n);
                coords.add(tc);
            }
        }
        return coords;
    }

    // Fill a triangle using the sizing and entry/exit semantics from above, where the triangle's origin is
    // the axial UV coordinate 'entry' and rotated clockwise by angle 60deg * rot
    public static ArrayList<DomePixel> fillTriangle(final PVector2 entry, final int rot, int n) {
        // TODO can these be derived from first principles?
        int[][] offsets = {{0, 0, -1}, {-1, 0, -1}, {-1, 0, 0}, {-1, -1, 0}, {0, -1, 0}, {0, -1, -1}};

        int u0 = (int)entry.x;
        int v0 = (int)entry.y;
        int[] o = offsets[MathUtil.mod(rot, 6)];
        TriCoord panel = TriCoord.fromParts(TriCoord.CoordType.PANEL,
          TriCoord.Axis.U, u0 + o[0], TriCoord.Axis.V, v0 + o[1],
          MathUtil.mod(rot, 2) == 0 ? TriCoord.PanelOrientation.A : TriCoord.PanelOrientation.B, 0);

        ArrayList<DomePixel> coords = new ArrayList<DomePixel>();
        for (TriCoord c : fillTriangle(n)) {
            coords.add(new DomePixel(panel, c.rotate(rot)));
        }
        return coords;
    }

    // Get the exit point for a triangle fill
    public static PVector2 exitPointForFill(PVector2 entry, int rot) {
        return axialNeighbor(entry, rot - 1);
    }

    public static ArrayList<DomePixel> fillFan(int orientation, int segments, int pixels) {
        return fillFan(orientation, segments, pixels, V(0, 0));
    }

    // Fill a fan of triangles proceeding in a clockwise fashion until a complete hexagon whose perimeter
    // intersects the origin is filled. 'segments' is the number of triangular segments to fill (up to 6).
    // 'pixels' is the fill density within each triangle. 'orientation' is the initial orientation in
    // which the long axis of the hexagon follows the angle specified by 'rot' semantics above.
    public static ArrayList<DomePixel> fillFan(int orientation, int segments, int pixels, PVector2 entry) {
        ArrayList<DomePixel> points = new ArrayList<DomePixel>();
        int rot = orientation;
        for (int i = 0; i < segments; i++) {
            points.addAll(fillTriangle(entry, rot, pixels));
            entry = exitPointForFill(entry, rot);
            rot += 1;
        }
        return points;
    }

    // Convert tri-grid u/v/w coordinates to cartesian x/y coordinates. Points are placed such that spacing
    // between two adjacent points will match the spacing between an edge point and the opposing point of a
    // neighboring triangle.
    public static PVector2 coordToXy(DomePixel c) {
        double spacing = pixelSpacing(c.pixel.panel_length);
        PVector2 root = c.panel.toV();
        PVector2 px = c.pixel.toV();
        PVector2 offset = V(1/SQRT_3, 1/SQRT_3);
        if (c.panel.getOrientation() == TriCoord.PanelOrientation.B) {
            root = LayoutUtil.Vadd(root, V(1, 1));
            px = LayoutUtil.Vsub(px, V(c.pixel.panel_length - 1, c.pixel.panel_length - 1));
            offset = LayoutUtil.Vmult(offset, -1);
        }
        return axialToXy(LayoutUtil.Vadd(root, LayoutUtil.Vmult(LayoutUtil.Vadd(px, offset), spacing)));
    }

    // All metadata associated with a particular layout of panels.
    public static abstract class PanelConfig {
        double radius;  // Max radius of panel configuration, in panel lengths
        int[] arms;     // Number of panels per fadecandy 'arm'
        PVector2 origin; // Center the layout on this point (in UV coordinates)
	PVector2 offset; // Origin in XY coordinates

        public PanelConfig(int num_panels, double radius, int[] arms, PVector2 origin) {
            this.radius = radius;
            this.arms = arms;
            this.origin = origin;
	    this.offset = axialToXy(origin);

            int panel_count = 0;
            for (int n : arms) {
                panel_count += n;
            }
            assert num_panels == panel_count;
        }

        // Fill the me.lsdo configuration with pixels
        abstract ArrayList<DomePixel> fill(int n);

	PixelTransform getDefaultTransform() {
	    return PixelTransform.simpleTransform(new LayoutUtil.Transform(){
		    public PVector2 transform(PVector2 p) {
			return LayoutUtil.Vmult(LayoutUtil.Vsub(p, offset), 1. / radius);
		    }
		});
	}
    }

    // Note: this layout is off-center.
    public static PanelConfig _2 = new PanelConfig(2,
                                            2./3.*SQRT_3,
                                            new int[] {2},
                                            V(1/3., 1/3.)) {
            ArrayList<DomePixel> fill(int n) {
                return fillFan(0, 2, n);
            }
        };

    public static PanelConfig _6 = new PanelConfig(6,
                                            1.,
                                            new int[] {4, 2},
                                            V(0, 0.)) {
            ArrayList<DomePixel> fill(int n) {
                ArrayList<DomePixel> points = new ArrayList<DomePixel>();
                points.addAll(fillFan(4, 4, n, V(-1, 1)));
                points.addAll(fillFan(5, 2, n, V(-1, 1)));
                return points;
            }
        };

    public static PanelConfig _13 = new PanelConfig(13,
                                             Math.sqrt(7/3.),  // just trust me
                                             new int[] {4, 4, 4, 1},
                                             V(1/3., 1/3.)) {
            ArrayList<DomePixel> fill(int n) {
                final PVector2[] entries = {V(1, 0), V(0, 1), V(0, 0)};
                ArrayList<DomePixel> points = new ArrayList<DomePixel>();
                for (int i = 0; i < 3; i++) {
                    points.addAll(fillFan(2*i+1, 4, n, entries[i]));
                }
                points.addAll(fillTriangle(V(0, 0), 0, n));
                return points;
            }
        };
    public static PanelConfig _24 = new PanelConfig(24,
                                             2.,
                                             new int[] {4, 4, 4, 4, 4, 4},
                                             V(0, 0)) {
            ArrayList<DomePixel> fill(int n) {
                ArrayList<DomePixel> points = new ArrayList<DomePixel>();
                for (int i = 0; i < 6; i++) {
                    points.addAll(fillFan(i, 4, n));
                }
                return points;
            }
        };

    public static PanelLayout getPanelLayoutForNumPanels(int numPanels) {
        switch (numPanels) {
        case 2:
            return PanelLayout._2;
        case 6:
            return PanelLayout._6;
        case 13:
            return PanelLayout._13;
        case 24:
            return PanelLayout._24;
        default:
            throw new RuntimeException(String.format("no defined layout for %d panels", numPanels));
        }
    }
    
    public static PanelConfig getPanelConfig(PanelLayout config) {
        switch (config) {
        case _2:
            return _2;
        case _6:
            return _6;
        case _13:
            return _13;
        case _24:
            return _24;
        default:
            throw new RuntimeException();
        }
    }

    // Convert a 2-vector of (U, V) coordinates from the axial coordinate scheme into (x, y) cartesian coordinates
    public static PVector2 axialToXy(PVector2 p) {
        PVector2 U = V(.5, .5 * SQRT_3);
        PVector2 V = V(1., 0.);
        return LayoutUtil.basisTransform(p, U, V);
    }

    // Convert a 2-vector of (U, V) coordinates from the axial coordinate scheme into (x, y) cartesian coordinates
    public static PVector2 xyToAxial(PVector2 p) {
	PVector2 U = V(0., 1.);
        PVector2 V = V(2. / SQRT_3, -1. / SQRT_3);
        return LayoutUtil.basisTransform(p, U, V);
    }

    // Return the adjacent axial coordinate moving from 'p' in direction 'rot'
    public static PVector2 axialNeighbor(PVector2 p, int rot) {
        int axis = MathUtil.mod(rot, 3);
        boolean hemi = (MathUtil.mod(rot, 6) < 3);
        int du = (axis == 0 ? 0 : (hemi ? -1 : 1));
        int dv = (axis == 2 ? 0 : (hemi ? 1 : -1));
        return V(p.x + du, p.y + dv);
    }

    // Return whether two axial coordinates are adjacent lattice points
    public static boolean axialCoordsAdjacent(PVector2 a, PVector2 b) {
        int du = (int)a.x - (int)b.x;
        int dv = (int)a.y - (int)b.y;
        return (du >= -1 && du <= 1 && dv >= -1 && dv <= 1 && du != dv);
    }

    // Number of pixels in a single panel of size n
    public static int pixelsPerPanel(int n) {
        return n * (n + 1) / 2;
    }

}
