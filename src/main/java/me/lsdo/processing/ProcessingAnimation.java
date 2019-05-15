package me.lsdo.processing;

import java.util.*;
import processing.core.PApplet;
import me.lsdo.processing.util.*;

/**
 * Animation that wraps a processing app/sketch and sources pixels from the sketch's canvas
 */
public class ProcessingAnimation extends WindowAnimation {

    protected PApplet app;

    public boolean stretchDefault() { return false; }

    // locations of mesh points to mark on processing canvas
    // use set to avoid duplicates, since we use xor to mark
    private Set<Integer> pixelPositions;
    
    public ProcessingAnimation(PApplet app, PixelMesh<? extends LedPixel> mesh){
        super(mesh);
        this.app = app;
	initViewport(app.width, app.height);
    }

    @Override
    public void captureFrame() {
        app.loadPixels();
    }

    @Override
    public int getPixel(int x, int y) {
	return app.pixels[linearOffset(x, y)];
    }

    @Override
    protected void postFrame(double t){
	for (int ix : pixelPositions) {
	    app.pixels[ix] = 0xFFFFFF ^ app.pixels[ix];
        }

        app.updatePixels();

        // on screen text
        app.fill(127f, 256f);
        app.text("opc @" + mesh.getOpcHosts(), 100, app.height - 10);
        app.text(String.format("%.1ffps", app.frameRate), 10, app.height - 10);
    }

    public void draw()
    {
        draw(app.millis() / 1000.);
    }

    @Override
    public void transformChanged() {
	super.transformChanged();
	initPixelPositions();
    }

    private void initPixelPositions() {
	pixelPositions = new HashSet<Integer>();
	for (LedPixel c : mesh.coords()) {
	    PVector2 screenP = LayoutUtil.xyToScreen(transform.transform(c), app.width, app.height);
	    int[] xy = boundsCheck(screenP);
	    if (xy != null) {
		pixelPositions.add(linearOffset(xy[0], xy[1]));
	    }
        }
    }

}
