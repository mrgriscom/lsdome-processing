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
import me.lsdo.processing.interactivity.*;
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
    private FlapManager flapper;
    
    // left and right are from the butterfly's perspective
    public Prometheus(OPC opcLeft, OPC opcRight) {
	super();	
	opcs.add(opcLeft);
	opcs.add(opcRight);
	mode = parseMode(Config.getSketchProperty("wing_mode", "unified"));
	init();
    }

    public static WingDisplayMode parseMode(String s) {
	if (s.equals("unified")) {
	    return WingDisplayMode.UNIFIED;
	} else if (s.equals("mirror")) {
	    return WingDisplayMode.MIRROR;
	} else if (s.equals("flip")) {
	    return WingDisplayMode.FLIP_HORIZ;
	} else if (s.equals("opposite")) {
	    return WingDisplayMode.ROTATE_180;
	} else {
	    throw new IllegalArgumentException("unrecognized wing mode '" + s + "'");
	}
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

	// Can't call this in the constructor because coords haven't been loaded yet, which affects the shape of the flap
	flapper = new FlapManager(this);
	// note that flapping just warps the projection transform -- it doesn't create a mask
	// for the original shape of the wing, meaning content initially 'off-wing' will move into
	// view. for processing-based sketches the source window forms a natural boundary, so it looks
	// fine, but for 'infinite canvas' headless sketches, the effect might look a bit weird.
	PixelTransform flapTx = flapper.getFlapTransform();

	return flapTx.compoundTransform(defaultTx);
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

    public void registerHandlers(InputControl ctrl) {
	super.registerHandlers(ctrl);
	flapper.registerHandlers(ctrl);
	
	final Prometheus mesh = this;
	
        ctrl.registerHandler("playpause_a", new InputControl.InputHandler() {
		@Override
                public void button(boolean pressed) {
                    if (pressed) {
			Prometheus.WingDisplayMode[] modes = Prometheus.WingDisplayMode.values();
			for (int i = 0; i < modes.length; i++) {
			    if (mesh.mode == modes[i]) {
				mesh.mode = modes[(i + 1) % modes.length];
				System.out.println("setting wing mode " + mesh.mode.name());
				break;
			    }
			}
			txChanged = true;
                    }
                }
            });
        ctrl.registerHandler("wingmode", new InputControl.InputHandler() {
		@Override
                public void set(String s) {
		    try {
			mesh.mode = Prometheus.parseMode(s);
			txChanged = true;
		    } catch (IllegalArgumentException e) {
			// ignore; leave mode as is
		    }
                }
            });
        ctrl.registerHandler("wingmode_unified", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			mesh.mode = Prometheus.WingDisplayMode.UNIFIED;
		    }
		    txChanged = true;
		}
            });
        ctrl.registerHandler("wingmode_mirror", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			mesh.mode = Prometheus.WingDisplayMode.MIRROR;
		    }
		    txChanged = true;
		}
            });
        ctrl.registerHandler("wingmode_flip", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			mesh.mode = Prometheus.WingDisplayMode.FLIP_HORIZ;
		    }
		    txChanged = true;
		}
            });
        ctrl.registerHandler("wingmode_rotate", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			mesh.mode = Prometheus.WingDisplayMode.ROTATE_180;
		    }
		    txChanged = true;
		}
            });	
    }

    public void beforeDraw(PixelMeshAnimation anim) {	
	if (flapper.manageState(anim)) {
	    txChanged = true;
	}
	super.beforeDraw(anim);
    }
    
}
