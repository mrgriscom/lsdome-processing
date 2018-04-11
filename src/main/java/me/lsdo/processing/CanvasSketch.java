package me.lsdo.processing;

import processing.core.PApplet;

import processing.data.FloatList;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.*;

/**
 * Created by shen on 2016/06/26.
 */
public class CanvasSketch extends XYAnimation {

    protected PApplet app;
    private static int DEFAULT_AA = 8;

    public CanvasSketch(PApplet app, PixelMesh<? extends LedPixel> dome){
        this(app, dome, Config.getSketchProperty("subsampling", DEFAULT_AA));
    }

    public CanvasSketch(PApplet app, PixelMesh<? extends LedPixel> dome, int antiAliasingSamples){
        super(dome, antiAliasingSamples);
        this.app = app;
    }

    @Override
    protected void preFrame(double t, double deltaT){
        app.loadPixels();
    }

    @Override
    protected void postFrame(double t){

        // draw pixel locations
	int[] ixs = new int[dome.coords.size()];
	for (int i = 0; i < ixs.length; i++) {
	    LedPixel c = dome.coords.get(i);
	    if (c.spacerPixel) {
		ixs[i] = -1;
	    } else {
		PVector2 screenP = dome.domeCoordToScreen(c, app.width, app.height);
		ixs[i] = coordToPixelIndex(screenP);
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
        app.text("opc @" + dome.getOpcHosts(), 100, app.height - 10);
        app.text(String.format("%.1ffps", app.frameRate), 10, app.height - 10);

    }

    int coordToPixelIndex(PVector2 p) {
	int x = (int)Math.floor(p.x);
	int y = (int)Math.floor(p.y);
	if (x < 0 || x >= app.width || y < 0 || y >= app.height) {
	    return -1;
	} else {
	    return y * app.width + x;
	}
    }
    
    @Override
    protected int samplePoint(PVector2 p, double t)
    {
	int i = coordToPixelIndex(p);
	return i >= 0 ? app.pixels[i] : 0;
    }

    // Store samples as screen coordinates.
    @Override
    protected PVector2 toIntermediateRepresentation(PVector2 p) {
	return LayoutUtil.xyToScreen(p, app.width, app.height);
    }
    
    public void draw()
    {
        draw(app.millis() / 1000.);
    }

}
