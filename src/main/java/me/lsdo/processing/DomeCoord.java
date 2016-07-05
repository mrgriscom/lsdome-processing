package me.lsdo.processing;

import java.util.*;
import me.lsdo.*;

// Representation of a coordinate within the dome pixel grid. See TriCoord.java for more info.

public class DomeCoord {

    // Every pixel in the dome has a unique coordinate.
    public TriCoord universal;

    // Universal coordinate split into a panel component...
    public TriCoord panel;

    // and a within-panel pixel component.
    public TriCoord pixel;

    public DomeCoord(TriCoord uni, int panel_length) {
        universal = uni;
        panel = TriCoord.toPanel(uni, panel_length);
        pixel = TriCoord.toPixel(uni, panel_length);
    }

    public DomeCoord(TriCoord panel, TriCoord pixel) {
        universal = TriCoord.toUniversal(panel, pixel);
        this.panel = panel;
        this.pixel = pixel;
    }

    TriCoord getCoord(TriCoord.CoordType type) {
        switch (type) {
        case UNIVERSAL: return universal;
        case PANEL: return panel;
        case PIXEL: return pixel;
        default: throw new RuntimeException();
        }
    }

    public boolean equals(Object o) {
        if (o instanceof DomeCoord) {
            DomeCoord dc = (DomeCoord)o;
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
