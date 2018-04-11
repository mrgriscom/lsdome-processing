package me.lsdo.processing;

public abstract class PixelTransform {

    static interface TransformListener {
	public void transformChanged();
    }
    
    public PVector2 transform(LedPixel px) {
	return transform(px, LayoutUtil.V(0, 0));
    }

    public abstract PVector2 transform(LedPixel px, PVector2 offset);

    public static PixelTransform simpleTransform(final LayoutUtil.Transform tx) {
	return new PixelTransform() {
	    public PVector2 transform(LedPixel px, PVector2 offset) {
		return tx.transform(LayoutUtil.Vadd(px.toXY(), offset));
	    }
	};
    }
    public PixelTransform compoundTransform(final LayoutUtil.Transform tx) {
	final PixelTransform baseTx = this;
	return new PixelTransform() {
	    public PVector2 transform(LedPixel px, PVector2 offset) {
		return tx.transform(baseTx.transform(px, offset));
	    }
	};
    }

    public PVector2 getMargins(LedPixel ref) {
    	// this assumes 'transform' is linear, and the same for all coords
	int radial_steps = 64;
	double xmargin = 0;
	double ymargin = 0;
	for (int i = 0; i < radial_steps; i++) {
	    PVector2 margin = LayoutUtil.polarToXy(LayoutUtil.V(1., (float)i / radial_steps * 2*Math.PI));
	    PVector2 txMargin = transform(ref, LayoutUtil.Vsub(margin, ref.toXY()));
	    xmargin = Math.max(xmargin, txMargin.x);
	    ymargin = Math.max(ymargin, txMargin.y);
	}
	return LayoutUtil.V(xmargin, ymargin);
    }

}

