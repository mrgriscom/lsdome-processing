

// Template for sketches where you want to directly assign a color to each pixel in the dome grid.
// The color assigned may depend on its triangular grid position or its xy position-- all that matters
// is that there is no underlying 'screen' being rendered that you want to pluck colors from.

package me.lsdo.processing;

import java.util.*;
import processing.core.*;

public abstract class PixelGridSketch<S> extends FadecandySketch<S> {

    protected HashMap<DomeCoord, Integer> pixelColors;

    public PixelGridSketch(PApplet app, int size_px) {
        super(app, size_px);
    }


    public void init() {
        super.init();

        pixelColors = new HashMap<DomeCoord, Integer>();
        for (DomeCoord c : coords) {
            pixelColors.put(c, 0x0);
        }
    }

    public void draw(double t) {
        app.background(0);
        app.loadPixels();
        for (int i = 0; i < coords.size(); i++) {
            setLED(i, drawPixel(coords.get(i), t));
        }
        app.updatePixels();
    }

    protected abstract int drawPixel(DomeCoord c, double t);
    // You can set the pixel colors in pixelColors in beforeFrame() and they will be automatically
    // rendered here. Or, you can override drawPixel() directly.
    //int drawPixel(DomeCoord c, double t) {
    //    return pixelColors.get(c);
    //}

}
