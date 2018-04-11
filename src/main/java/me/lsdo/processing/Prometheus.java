/**
 * The abstract structure of the dome. No aware of any sketches.
 *
 * Config is pulled in from elsewhere, and the layout is mostly done by layout util, so class is a little bit
 * thin right now.
 */

package me.lsdo.processing;

import java.util.*;
import java.io.*;
import com.google.gson.*;
import com.google.gson.stream.*;

enum WingDisplayMode {
    UNIFIED,
    MIRROR,
    FLIP_HORIZ,
    ROTATE_180
};

public class Prometheus extends PixelMesh<WingPixel> {

    static final String LAYOUT_PATH = "/home/drew/dev/lsdome/lsdome/src/config/simulator_layouts/prometheus_wing.json";
    static final double PLATFORM_WIDTH = 1.; // m
    static final double WINGSPAN = 18.; // m
    
    static class LayoutPoint {
	double[] point;
    }

    WingDisplayMode mode;
    
    // left and right are from the butterfly's perspective
    public Prometheus(OPC opcLeft, OPC opcRight) {
	super();

	opcs.add(opcLeft);
	opcs.add(opcRight);

	mode = WingDisplayMode.UNIFIED;
	
	List<PVector2> pixels;
	try {
	    pixels = loadPixels(LAYOUT_PATH);
	} catch (IOException e) {
	    throw new RuntimeException("can't load wing pixel layout json");
	}
	for (int i = 0; i < pixels.size(); i++) {
	    for (int wing = 0; wing < 2; wing++) {
		coords.add(new WingPixel(wing, i, pixels.get(i)));
	    }
	}

	// initial transform creates a mirrored wing if both wings are meant to be
	// fixed relative to each other (as in UNIFIED mode)
	transform = new PixelTransform() {
		public PVector2 transform(LedPixel px, PVector2 offset) {
		    PVector2 p = px.toXY();
		    if (mode == WingDisplayMode.UNIFIED && ((WingPixel)px).wing == 1) {
			p = LayoutUtil.V(-p.x, p.y);
		    }
		    p = LayoutUtil.Vadd(p, offset);

		    return LayoutUtil.Vmult(p, 2./WINGSPAN);
		}
	    };
	// save this *before* init()
	final PixelTransform baseTx = transform;
	
	init();

	// do this after init() to overwrite the compound transform created there (this is
	// janky and should be refactored, obv)
	// for the applicable mode, this transform mirrors the 2nd wing from the 1st in various ways
	transform = new PixelTransform() {
		public PVector2 transform(LedPixel px, PVector2 offset) {
		    PVector2 p = baseTx.transform(px, offset);
		    p = placement.transform(p);
		    if (((WingPixel)px).wing == 1) {
			if (mode == WingDisplayMode.FLIP_HORIZ) {
			    p = LayoutUtil.V(-p.x, p.y);
			} else if (mode == WingDisplayMode.ROTATE_180) {
			    p = LayoutUtil.V(-p.x, -p.y);
			}
		    }
		    return p;
		}
	    };
    }

    private List<PVector2> loadPixels(String path) throws IOException {
	Gson gson = new Gson();
	InputStream is = new BufferedInputStream(new FileInputStream(new File(path)));
	JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));

        List<PVector2> points = new ArrayList<PVector2>();
        reader.beginArray();
        while (reader.hasNext()) {
	    LayoutPoint lp = gson.fromJson(reader, LayoutPoint.class);
	    double x = lp.point[0];
	    double y = lp.point[1];
	    double z = lp.point[2];
	    if (z != 0 || (x == -5 && y == -5) /* old method*/) {
		// spacer pixel
		points.add(null);
	    } else {
		points.add(LayoutUtil.V(x, y));
	    }
        }
        reader.endArray();
        reader.close();

	double minX = Double.POSITIVE_INFINITY;
	for (PVector2 p : points) {
	    if (p != null) {
		minX = Math.min(minX, p.x);
	    }
	}
        List<PVector2> realignedPoints = new ArrayList<PVector2>();
	for (PVector2 p : points) {
	    if (p == null) {
		// spacer pixel
		realignedPoints.add(null);
	    } else {
		// Note: assuming y-axis is centered properly (but reversed)
		realignedPoints.add(LayoutUtil.V(p.x - minX + .5*PLATFORM_WIDTH, -p.y));
	    }
	}
        return realignedPoints;
    }
    
    public int getOpcChannel(WingPixel pixel) {
	return pixel.wing;
    }
    
    public double getPixelBufferRadius() {
	double spacing = .15; // m
	return .5 * spacing * .7; // reduce to 70% to account for denser areas of wing
    }

}
