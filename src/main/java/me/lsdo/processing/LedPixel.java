package me.lsdo.processing;

import me.lsdo.processing.util.*;

// A physical pixel to be rendered to.

public abstract class LedPixel {

    public boolean spacerPixel = false;
    
    // Return the XY position of this pixel, in meters
    public abstract PVector2 toXY();
    
}
