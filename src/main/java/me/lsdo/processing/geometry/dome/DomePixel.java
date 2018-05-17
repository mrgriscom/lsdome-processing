package me.lsdo.processing.geometry.dome;

import me.lsdo.processing.LedPixel;
import me.lsdo.processing.util.*;

// Representation of a coordinate within the dome pixel grid. See TriCoord.java for more info.

public class DomePixel extends LedPixel {

    // Every pixel in the dome has a unique coordinate.
    public TriCoord universal;

    // Universal coordinate split into a panel component...
    public TriCoord panel;

    // and a within-panel pixel component.
    public TriCoord pixel;

    public DomePixel(TriCoord uni, int panel_length) {
        universal = uni;
        panel = TriCoord.toPanel(uni, panel_length);
        pixel = TriCoord.toPixel(uni, panel_length);
    }

    public DomePixel(TriCoord panel, TriCoord pixel) {
        universal = TriCoord.toUniversal(panel, pixel);
        this.panel = panel;
        this.pixel = pixel;
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
	return DomeLayoutUtil.coordToXy(this);
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
