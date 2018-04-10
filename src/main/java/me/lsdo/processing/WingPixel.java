package me.lsdo.processing;

// Representation of a coordinate within the dome pixel grid. See TriCoord.java for more info.

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
	    this.xy = LayoutUtil.V(0, 0);
	    this.spacerPixel = true;
	}
    }

    public PVector2 toXY() {
	return wing == 0 ? xy : LayoutUtil.V(-xy.x, xy.y);
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
