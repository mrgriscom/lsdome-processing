package me.lsdo.processing;

// A physical pixel to be rendered to.

public abstract class LedPixel {

    // Return the XY position of this pixel, in meters
    public abstract PVector2 toXY();
    
}
