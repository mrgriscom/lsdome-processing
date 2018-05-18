package me.lsdo.processing.geometry.prometheus;

import java.util.*;
import java.io.*;
import com.google.gson.*;
import com.google.gson.stream.*;
import me.lsdo.processing.LedPixel;
import me.lsdo.processing.PixelMesh;
import me.lsdo.processing.PixelMeshAnimation;
import me.lsdo.processing.PixelTransform;
import me.lsdo.processing.OPC;
import me.lsdo.processing.util.*;

// Prometheus geometry. Reads first wing's pixel positions from simulator json config. Maps 2nd wing
// relative to 1st wing according to several different modes.

public class Prometheus extends PixelMesh<WingPixel> {

    // How to map 2nd wing relative to 1st wing
    public static enum WingDisplayMode {
	// Keep the 2nd wing locked relative to the 1st wing, to match how they appear in real life; transform them as a single unit
	UNIFIED,
	// Mirror the 1st wing to the 2nd wing exactly (reverses display on 2nd wing)
	MIRROR,
	// Flip the 1st wing along the horizontal axis of the screen to get the placement of the 2nd wing
	FLIP_HORIZ,
	// Rotate the 1st wing 180deg around the canvas origin to get the placement of the 2nd wing
	ROTATE_180
    }

    static final double PLATFORM_WIDTH = 1.; // m
    static final double WINGSPAN = 15.5; // m (factors in platform_width)

    // for deserializing from json
    static class LayoutPoint {
	double[] point;
    }

    public WingDisplayMode mode;
    
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
	
	init();
    }

    protected List<WingPixel> getCoords() {
	String layoutPath = Config.getConfig().layoutPath;
	if (layoutPath.isEmpty()) {
	    throw new RuntimeException("json layout not specified in config 'layout' property");
	}
	
	List<PVector2> coords;
	try {
	    coords = loadPixels(layoutPath);
	} catch (IOException e) {
	    throw new RuntimeException("can't load wing pixel layout json at " + layoutPath);
	}
	List<WingPixel> pixels = new ArrayList<WingPixel>();
	for (int i = 0; i < coords.size(); i++) {
	    for (int wing = 0; wing < 2; wing++) {
		pixels.add(new WingPixel(wing, i, coords.get(i)));
	    }
	}
	return pixels;
    }

    protected PixelTransform getDefaultTransform() {
	// initial transform creates a mirrored wing if both wings are meant to be
	// fixed relative to each other (as in UNIFIED mode)
	PixelTransform defaultTx = new PixelTransform() {
		public PVector2 transform(LedPixel px, PVector2 p) {
		    if (mode == WingDisplayMode.UNIFIED && ((WingPixel)px).wing == 1) {
			p = LayoutUtil.V(-p.x, p.y);
		    }
		    return LayoutUtil.Vmult(p, 2./WINGSPAN);
		}
	    };

	setFlapAngle(this.flapAngle);
	// note that flapping just warps the projection transform -- it doesn't create a mask
	// for the original shape of the wing, meaning content initially 'off-wing' will move into
	// view. for processing-based sketches the source window forms a natural boundary, so it looks
	// fine, but for 'infinite canvas' headless sketches, the effect might look a bit weird.
	PixelTransform flapper = new PixelTransform() {
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

	return flapper.compoundTransform(defaultTx);
    }
    
    protected PixelTransform getPostPlacementTransform() {
	// for the applicable mode, this transform mirrors the 2nd wing from the 1st in various ways
	return new PixelTransform() {
	    public PVector2 transform(LedPixel px, PVector2 p) {
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

    ////////// FLAPPING
    
    // flapping parameter limits
    public double maxMaxFlap = .01;
    public double minMaxFlap = .5;
    public double minFlapAngle = Math.toRadians(-10);
    public double maxFlapAngle = Math.toRadians(20);
    public double minFlapPeriod = .25;
    public double maxFlapPeriod = 2;

    // flapping parameters
    public double flapPeriod = .5; // seconds
    public double maxFlap = maxMaxFlap; // percentage
    double flapAngle = 0; // radians
    double flapVanishingPointOffset = .25; // meters
    
    // flapping instantaneous state variables
    double flappingStart = -1; // timestamp
    double flappingEnd = -1; // timestamp
    double flapLevel = 1.; // percentage
    double flapOrigin;
    boolean isFlapping = false;
    
    public void setFlapAngle(double flapAngle) {
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
    
    public void startFlapping() {
	if (!flappingActive()) {
	    flappingStart = Config.clock();
	}
	flappingEnd = -1;
    }

    public void stopFlapping() {
	flappingEnd = flappingStart + flapPeriod * Math.ceil((Config.clock() - flappingStart) / flapPeriod);
    }

    boolean flappingActive() {
	return (flappingStart >= 0 && (flappingEnd < 0 || Config.clock() < flappingEnd));
    }

    double flapEasing(double x) {
	return MathUtil.sineEasing(x);
    }
    
    public boolean manageFlapState(PixelMeshAnimation anim) {
	boolean active = flappingActive();
	if (active) {
	    double flapProgress = ((Config.clock() - flappingStart) / flapPeriod) % 1.; // 0 to 1
	    double easingX = 1 - Math.abs(2*flapProgress - 1); // 0 to 1 to 0
	    flapLevel = 1 - flapEasing(easingX); // 1 to 0 to 1
	    flapLevel = maxFlap + flapLevel * (1 - maxFlap);
	} else {
	    flapLevel = 1.;
	}
	if (anim instanceof PixelTransform.TransformListener) {
	    ((PixelTransform.TransformListener)anim).transformAnimating(active);
	}
	boolean changed = (active != isFlapping);
	isFlapping = active;
	return active || changed;
    }

    
}
