package me.lsdo.processing;

import java.util.*;
import me.lsdo.processing.interactivity.*;
import me.lsdo.processing.util.*;

// Animations that sample from a pre-rendered window / 2d array of pixels.

public abstract class WindowAnimation extends XYAnimation {

    // window width in pixels
    int width;
    // window height in pixels
    int height;
    // if true, stretch the mesh geometry independently in the x- and y-axes to maximally cover the window
    public BooleanParameter stretchAspect;
    public boolean stretchDefault() { return true; }
    // downscale the window to squeeze more window area onto geometry with irregular edges, at the expense
    // of leaving some of the geometry blank / outside of the window area
    public NumericParameter xscale;
    public NumericParameter yscale;
    // allow moving the viewport within the geometry as a final adjustment after all other transforms
    // have taken place
    public NumericParameter xo;
    public NumericParameter yo;
    // sometimes the content must be squeezed to fit the window; if so, this represents the
    // true aspect ratio of the original content
    double aspectRatio;
    
    public PixelTransform windowTransform;
    PixelTransform transform;

    public double outsideViewportProportion;

    public static final int DEFAULT_AA = 8;

    public WindowAnimation(final PixelMesh<? extends LedPixel> mesh) {
	this(mesh, DEFAULT_AA);
    }
    
    public WindowAnimation(final PixelMesh<? extends LedPixel> mesh, int antiAliasingSamples) {
        super(mesh, antiAliasingSamples);

	transform = mesh.transform.compoundTransform(
            // need a closure as windowTransform is a new object every time it's changed
	    new PixelTransform() {
		public PVector2 transform(PVector2 p) {
		    return windowTransform.transform(p);
		}
	    });

	stretchAspect = mesh.new BoolPlacementParameter("stretch aspect");
	stretchAspect.trueCaption = "stretch to fit window";
	stretchAspect.falseCaption = "preserve 1:1";
	stretchAspect.init(!Config.getSketchProperty("no_stretch", !stretchDefault()));

	xscale = mesh.new PlacementParameter("x-scale");
	xscale.min = .5;
	xscale.max = 3.;
	xscale.init(Config.getSketchProperty("placement_xscale", 1.));
	yscale = mesh.new PlacementParameter("y-scale");
	yscale.min = .5;
	yscale.max = 3.;
	yscale.init(Config.getSketchProperty("placement_yscale", 1.));
	
	xo = mesh.new PlacementParameter("post-stretch x-offset");
	xo.setSensitivity(.01);
	xo.init(Config.getSketchProperty("placement_xo_poststretch", 0.));
	yo = mesh.new PlacementParameter("post-stretch y-offset");
	yo.setSensitivity(.01);
	yo.init(Config.getSketchProperty("placement_yo_poststretch", 0.));
    }

    public double getWindowAspectRatio() {
	return (double)width / height;
    }
    
    public void initViewport(int width, int height) {
	initViewport(width, height, 0.);
    }
    
    public void initViewport(int width, int height, double realAspectRatio) {
	this.width = width;
	this.height = height;
	this.aspectRatio = (realAspectRatio > 0 ? realAspectRatio : getWindowAspectRatio());
	mesh.txChanged = true;
	
	InputControl.AspectRatioJson ar = new InputControl.AspectRatioJson();
	ar.aspect = this.aspectRatio;
	ctrl.broadcast(ar);
    }

    @Override
    public void transformChanged() {
	if (!transformIsAnimating) {
	    if (stretchAspect.get()) {
		windowTransform = mesh.stretchToViewport(width, height, 1./xscale.get(), 1./yscale.get(), xo.get(), yo.get());
	    } else {
		windowTransform = new PixelTransform() {
			public PVector2 transform(PVector2 p) {
			    double aspectCorrection = aspectRatio / getWindowAspectRatio();
			    return LayoutUtil.V(p.x / aspectCorrection, p.y);
			}
		    };
	    }
	}
	applyTransform(transform);
	outsideViewportProportion = proportionOutsideBounds();
	if (outsideViewportProportion > 0) {
	    System.out.println(String.format("%.2f%% outsize window area!", 100. * outsideViewportProportion));
	}

    }

    public abstract void captureFrame();
    public abstract int getPixel(int x, int y);
    
    @Override
    protected void preFrame(double t, double deltaT) {
	long start = System.currentTimeMillis();
	captureFrame();
	long end = System.currentTimeMillis();
	//System.out.println(String.format("capture: %d ms   framerate: %.1f", end - start, frameRate));
    }

    // store samples as window pixel coordinates
    @Override
    protected PVector2 toIntermediateRepresentation(PVector2 p) {
	return LayoutUtil.xyToScreen(p, width, height);
    }

    @Override
    protected int samplePoint(PVector2 p, double t) {
	int[] c = boundsCheck(p);
	return c != null ? getPixel(c[0], c[1]) : 0;
    }

    int[] boundsCheck(PVector2 screenP) {
	int x = (int)Math.floor(screenP.x);
	int y = (int)Math.floor(screenP.y);
	if (x < 0 || x >= width || y < 0 || y >= height) {
	    return null;
	} else {
	    return new int[] {x, y};
	}
    }

    int linearOffset(int x, int y) {
	return width * y + x;
    }

    double proportionOutsideBounds() {
	int total = 0;
	int oob = 0;
	for (Map.Entry<LedPixel, ArrayList<PVector2>> e : points_ir.entrySet()) {
	    for (PVector2 p : e.getValue()) {
		total++;
		if (boundsCheck(p) == null) {
		    oob++;
		}
	    }
	}
	return (double)oob / total;
    }
}

