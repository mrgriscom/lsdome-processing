package me.lsdo.processing;

// A physical pixel to be rendered to.

public abstract class LedPixel {

    public boolean spacerPixel = false;
    
    // Return the XY position of this pixel, in meters
    public abstract PVector2 toXY();
    
}
