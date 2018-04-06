package me.lsdo.processing;

// Representation of a coordinate within the dome pixel grid. See TriCoord.java for more info.

public class DomePixel extends LedPixel {

    // Every pixel in the dome has a unique coordinate.
    public TriCoord universal;

    // Universal coordinate split into a panel component...
    public TriCoord panel;

    // and a within-panel pixel component.
    public TriCoord pixel;

    // temporary -- these should be moved to a separate transform in XYAnimation
    PVector2 offset;
    double theta;
    
    public DomePixel(TriCoord uni, int panel_length) {
        universal = uni;
        panel = TriCoord.toPanel(uni, panel_length);
        pixel = TriCoord.toPixel(uni, panel_length);
    }

    public DomePixel(TriCoord panel, TriCoord pixel, PVector2 origin, double theta) {
        universal = TriCoord.toUniversal(panel, pixel);
        this.panel = panel;
        this.pixel = pixel;

	this.offset = DomeLayoutUtil.axialToXy(origin);
	this.theta = theta;
    }

    public TriCoord getCoord(TriCoord.CoordType type) {
        switch (type) {
        case UNIVERSAL: return universal;
        case PANEL: return panel;
        case PIXEL: return pixel;
        default: throw new RuntimeException();
        }
    }

    public PVector2 toXY() {
	return LayoutUtil.Vrot(LayoutUtil.Vsub(DomeLayoutUtil.coordToXy(this), offset), Math.toRadians(theta));
    }
    
    public boolean equals(Object o) {
        if (o instanceof DomePixel) {
            DomePixel dc = (DomePixel)o;
            return universal.equals(dc.universal);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return universal.hashCode();
    }

    public String toString() {
        return String.format("[uni:%s panel:%s pixel:%s", universal, panel, pixel);
    }

}
