package me.lsdo.processing;

// A physical pixel to be rendered to.

public abstract class LedPixel {

    // Return which of N OPC servers manages this pixel
    public int getOpcChannel() {
	return 0;
    }

    // Return the XY position of this pixel, in meters
    public abstract PVector2 toXY();
    
}
