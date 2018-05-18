package me.lsdo.processing;

import me.lsdo.processing.util.*;

public abstract class PixelTransform {

    public final PVector2 transform(LedPixel px) {
	return transform(px, px.toXY());
    }

    // Override this instead of transform() if you want to vary the transformation based on
    // properties of the pixel
    public PVector2 transform(LedPixel px, PVector2 xy) {
	return transform(xy);
    }

    // Override this if the transform depends solely on the coordinate
    public PVector2 transform(PVector2 xy) {
	throw new RuntimeException("not implemented");
    }

    // return a new transform that first applies this transform, then 'tx'
    public PixelTransform compoundTransform(final PixelTransform tx) {
	final PixelTransform baseTx = this;
	return new PixelTransform() {
	    public PVector2 transform(LedPixel px, PVector2 xy) {
		return tx.transform(px, baseTx.transform(px, xy));
	    }

	    public PVector2 transform(PVector2 xy) {
		return tx.transform(baseTx.transform(xy));
	    }
	};
    }

    public static interface TransformListener {
	public void transformChanged();

	// called when the transform starts/stops animating, i.e., is expected to be
	// changing every frame
	public void transformAnimating(boolean yes);
    }
    
    public PVector2 getMargins(LedPixel ref) {
    	// this assumes 'transform' is linear, and the same for all coords
	int radial_steps = 64;
	double xmargin = 0;
	double ymargin = 0;
	for (int i = 0; i < radial_steps; i++) {
	    PVector2 margin = LayoutUtil.polarToXy(LayoutUtil.V(1., (float)i / radial_steps * 2*Math.PI));
	    PVector2 txMargin = transform(ref, margin);
	    xmargin = Math.max(xmargin, txMargin.x);
	    ymargin = Math.max(ymargin, txMargin.y);
	}
	return LayoutUtil.V(xmargin, ymargin);
    }

}

