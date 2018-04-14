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

    static final double PLATFORM_WIDTH = 1.; // m
    static final double WINGSPAN = 18.; // m
    
    static class LayoutPoint {
	double[] point;
    }

    WingDisplayMode mode;

    double flapPeriod = .5; // seconds
    double maxFlap = .01; // percentage
    double flapAngle = 0; // radians
    double minFlapAngle = Math.toRadians(-10);
    double maxFlapAngle = Math.toRadians(20);
    double flapVanishingPointOffset = .25; // meters
    
    // state variables
    double flappingStart = -1; // timestamp
    double flappingEnd = -1; // timestamp
    double flapLevel = 1.; // percentage
    double flapOrigin;
    
    void setFlapAngle(double flapAngle) {
	this.flapAngle = flapAngle;

	double min = Double.POSITIVE_INFINITY;
	for (LedPixel px : coords) {
	    if (px.spacerPixel) {
		continue;
	    }
	    
	    double x = LayoutUtil.Vrot(px.toXY(), flapAngle).x;
	    min = Math.min(min, x);
	}
	flapOrigin = min - flapVanishingPointOffset;
    }
    
    void startFlapping() {
	if (!flappingActive()) {
	    flappingStart = Config.clock();
	}
	flappingEnd = -1;
    }

    void stopFlapping() {
	flappingEnd = flappingStart + flapPeriod * Math.ceil((Config.clock() - flappingStart) / flapPeriod);
    }

    boolean flappingActive() {
	return (flappingStart >= 0 && (flappingEnd < 0 || Config.clock() < flappingEnd));
    }
    
    // some curve connecting (0,0) to (1,1)
    double flapEasing(double x) {
	// linear
	//return x;

	// sinusoidal
	return .5*(1 - Math.cos(x * Math.PI));

	// polynomial
	//double power = 3;
	//double k = Math.pow(1 - Math.abs(2*x - 1), power);
	//return (x < .5 ? .5*k : 1 - .5*k);
    }

    boolean manageFlapState() {
	if (!flappingActive()) {
	    flapLevel = 1.;
	    return false;
	}
	
	double flapProgress = ((Config.clock() - flappingStart) / flapPeriod) % 1.; // 0 to 1
	double easingX = 1 - Math.abs(2*flapProgress - 1); // 0 to 1 to 0
	flapLevel = 1 - flapEasing(easingX); // 1 to 0 to 1
	flapLevel = maxFlap + flapLevel * (1 - maxFlap);
	
	return true;
    }
    
    // left and right are from the butterfly's perspective
    public Prometheus(OPC opcLeft, OPC opcRight) {
	super();

	opcs.add(opcLeft);
	opcs.add(opcRight);

	String modeSetting = Config.getSketchProperty("wing_mode", "unified");
	if (modeSetting.equals("unified")) {
	    mode = WingDisplayMode.UNIFIED;
	} else if (modeSetting.equals("mirror")) {
	    mode = WingDisplayMode.MIRROR;
	} else if (modeSetting.equals("flip")) {
	    mode = WingDisplayMode.FLIP_HORIZ;
	} else if (modeSetting.equals("opposite")) {
	    mode = WingDisplayMode.ROTATE_180;
	} else {
	    throw new RuntimeException("unrecognized wing mode '" + modeSetting + "'");
	}

	String layoutPath = Config.getConfig().layoutPath;
	if (layoutPath.isEmpty()) {
	    throw new RuntimeException("json layout not specified in config 'layout' property");
	}
	
	List<PVector2> pixels;
	try {
	    pixels = loadPixels(layoutPath);
	} catch (IOException e) {
	    throw new RuntimeException("can't load wing pixel layout json at " + layoutPath);
	}
	for (int i = 0; i < pixels.size(); i++) {
	    for (int wing = 0; wing < 2; wing++) {
		coords.add(new WingPixel(wing, i, pixels.get(i)));
	    }
	}

	setFlapAngle(this.flapAngle);
	
	final LayoutUtil.Transform flapper = new LayoutUtil.Transform() {
		public PVector2 transform(PVector2 p) {
		    if (flapLevel == 1.) {
			return p;
		    } else {
			p = LayoutUtil.Vrot(p, flapAngle);
			p = LayoutUtil.V((p.x - flapOrigin) / Math.max(flapLevel, .01) + flapOrigin, p.y);
			p = LayoutUtil.Vrot(p, -flapAngle);
			return p;
		    }
		}
	    };	
	// initial transform creates a mirrored wing if both wings are meant to be
	// fixed relative to each other (as in UNIFIED mode)
	transform = new PixelTransform() {
		public PVector2 transform(LedPixel px, PVector2 offset) {
		    PVector2 p = px.toXY();
		    p = LayoutUtil.Vadd(p, offset);
		    p = flapper.transform(p);
		    if (mode == WingDisplayMode.UNIFIED && ((WingPixel)px).wing == 1) {
			p = LayoutUtil.V(-p.x, p.y);
		    }
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
	    if (z != 0) {
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
