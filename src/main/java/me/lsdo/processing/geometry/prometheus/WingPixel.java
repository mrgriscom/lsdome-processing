package me.lsdo.processing.geometry.prometheus;

import me.lsdo.processing.LedPixel;
import me.lsdo.processing.util.*;

// Pixel on a prometheus wing

public class WingPixel extends LedPixel {

    public int wing;
    public int i; // TODO add segment, row?
    public PVector2 xy;
    
    public WingPixel(int wing, int i, PVector2 xy) {
	this.wing = wing;
	this.i = i;
	if (xy != null) {
	    this.xy = xy;
	} else {
	    this.spacerPixel = true;
	}
    }

    protected PVector2 _toXY() {
	return xy;
    }
    
    public boolean equals(Object o) {
        if (o instanceof WingPixel) {
            WingPixel wp = (WingPixel)o;
	    return this.wing == wp.wing && this.i == wp.i;
        } else {
            return false;
        }
    }

    public int hashCode() {
	int hash = 1;
	hash = 31 * hash + wing;
	hash = 31 * hash + i;
	return hash;
    }

    public String toString() {
	return wing + ":" + i;
    }

}
