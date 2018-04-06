

// Template for sketches where you want to directly assign a color to each pixel in the dome grid.
// The color assigned may depend on its triangular grid position or its xy position-- all that matters
// is that there is no underlying 'screen' being rendered that you want to pluck colors from.

// This is the 'bridge' between headless mode and the processing UI. These sketches don't actually
// need processing to run (i.e., can run headless), but this sketch writes the content back to the
// processing canvas for visualization.

package me.lsdo.processing;

import java.util.*;
import processing.core.PApplet;

public class PixelGridSketch {

    protected PApplet app;
    protected DomeAnimation<? extends LedPixel> animation;

    private Map<LedPixel, PVector2> screenCoords;
    
    public PixelGridSketch(PApplet app, DomeAnimation<? extends LedPixel> animation) {
        this.app = app;
        this.animation = animation;

	// Pre-compute screen coordinates for dome pixels.
	screenCoords = new HashMap<LedPixel, PVector2>();
        for (LedPixel c : animation.dome.coords) {
            PVector2 screenCoord = animation.dome.domeCoordToScreen(c, app.width, app.height);
	    screenCoords.put(c, screenCoord);
        }
    }

    public void draw() {
        double t = app.millis()/1000d;

        animation.draw(t);

        app.background(0);
        app.noStroke();
        for (LedPixel c : animation.dome.coords){
            PVector2 p = screenCoords.get(c);
            app.fill(animation.dome.getColor(c));
            app.ellipse(p.x, p.y, 3, 3);
        }

        app.fill(127);
        app.text("opc @" + animation.dome.getOpcHosts(), 100, app.height - 10);
        app.text(String.format("%.1ffps", app.frameRate), 10, app.height - 10);

    }

}
