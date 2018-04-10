package me.lsdo.processing;

public abstract class PixelTransform {

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

}

