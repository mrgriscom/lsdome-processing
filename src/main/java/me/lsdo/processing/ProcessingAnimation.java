package me.lsdo.processing;

import java.util.*;
import processing.core.PApplet;
import me.lsdo.processing.util.*;

/**
 * Animation that wraps a processing app/sketch and sources pixels from the sketch's canvas
 */
public class ProcessingAnimation extends WindowAnimation {

    protected PApplet app;
    private static int DEFAULT_AA = 8;

    public ProcessingAnimation(PApplet app, PixelMesh<? extends LedPixel> mesh){
        this(app, mesh, Config.getSketchProperty("subsampling", DEFAULT_AA));
    }

    public ProcessingAnimation(PApplet app, PixelMesh<? extends LedPixel> mesh, int antiAliasingSamples){
        super(mesh, antiAliasingSamples);
        this.app = app;
	initViewport(app.width, app.height, true);
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

	// the screen positions of the pixels could be computed once per transform change for
	// increased efficiency, but we seem to be doing ok as-is
	
        // draw pixel locations onto the processing canvas for clarity
	int[] ixs = new int[mesh.coords.size()];
	for (int i = 0; i < ixs.length; i++) {
	    LedPixel c = mesh.coords.get(i);
	    if (c.spacerPixel) {
		ixs[i] = -1;
	    } else {
		PVector2 screenP = LayoutUtil.xyToScreen(transform.transform(c), app.width, app.height);
		int[] xy = boundsCheck(screenP);
		ixs[i] = xy != null ? linearOffset(xy[0], xy[1]) : -1;
	    }
        }
	Arrays.sort(ixs); // do this to catch duplicates -- since we use xor, if two points occupy the same pixel it will not be marked
	for (int i = 0; i < ixs.length; i++) {
	    int ix = ixs[i];
	    if (ix < 0) {
		continue;
	    }
	    if (i > 0 && ix == ixs[i - 1]) {
		continue;
	    }
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

}
