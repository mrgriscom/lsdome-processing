package me.lsdo.processing;

import me.lsdo.processing.util.*;

// A physical pixel to be rendered to.

public abstract class LedPixel {

    public boolean spacerPixel = false;

    public PVector2 toXY() {
	return spacerPixel ? null : _toXY();
    }
    
    // Return the XY position of this pixel, in meters
    protected abstract PVector2 _toXY();
    
}
