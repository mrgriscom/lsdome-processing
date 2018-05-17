package me.lsdo.processing;

import java.util.*;

// Animations that sample from a pre-rendered window / 2d array of pixels.

public abstract class WindowAnimation extends XYAnimation {

    // window width in pixels
    int width;
    // window height in pixels
    int height;
    // if false, stretch the mesh geometry independently in the x- and y-axes to maximally cover the window
    public boolean preserveAspect;
    // downscale the window to squeeze more window area onto geometry with irregular edges, at the expense
    // of leaving some of the geometry blank / outside of the window area
    double xscale;
    double yscale;
    // sometimes the content must be squeezed to fit the window; if so, this represents the
    // true aspect ratio of the original content
    double aspectRatio;
    
    public LayoutUtil.Transform windowTransform;
    PixelTransform transform;

    public double outsideViewportProportion;
    
    public WindowAnimation(final PixelMesh<? extends LedPixel> mesh, int antiAliasingSamples) {
        super(mesh, antiAliasingSamples);

	transform = new PixelTransform() {
		public PVector2 transform(LedPixel px, PVector2 offset) {
		    return windowTransform.transform(mesh.transform.transform(px, offset));
		}
	    };
    }

    public double getWindowAspectRatio() {
	return (double)width / height;
    }
    
    public void initViewport(int width, int height, boolean preserveAspect) {
	initViewport(width, height, preserveAspect, 0.);
    }
    
    public void initViewport(int width, int height, boolean preserveAspect, double realAspectRatio) {
	initViewport(width, height, preserveAspect, realAspectRatio, 1., 1.);
    }
    
    public void initViewport(int width, int height, boolean preserveAspect, double realAspectRatio, double xscale, double yscale) {
	this.width = width;
	this.height = height;
	this.preserveAspect = preserveAspect;
	this.xscale = xscale;
	this.yscale = yscale;
	this.aspectRatio = (realAspectRatio > 0 ? realAspectRatio : getWindowAspectRatio());
    }

    @Override
    public void transformChanged() {
	if (preserveAspect) {
	    windowTransform = new LayoutUtil.Transform() {
		    public PVector2 transform(PVector2 p) {
			double aspectCorrection = aspectRatio / getWindowAspectRatio();
			return LayoutUtil.V(p.x / aspectCorrection, p.y);
		    }
		};
	} else if (!transformIsDynamic) {
	    windowTransform = mesh.stretchToViewport(width, height, xscale, yscale);
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

