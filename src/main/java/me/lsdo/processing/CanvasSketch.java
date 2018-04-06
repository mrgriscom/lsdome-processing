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
        for (LedPixel c : dome.coords){
	    PVector2 screenP = dome.domeCoordToScreen(c, app.width, app.height);
            int pixelLocation = (int) Math.floor(screenP.x) + app.width * ((int) Math.floor(screenP.y));

            app.pixels[pixelLocation] = 0xFFFFFF ^ app.pixels[pixelLocation];
        }

        app.updatePixels();

        // on screen text
        app.fill(127f, 256f);
        app.text("opc @" + dome.getOpcHosts(), 100, app.height - 10);
        app.text(String.format("%.1ffps", app.frameRate), 10, app.height - 10);

    }

    @Override
    protected int samplePoint(PVector2 screenP, double t)
    {
        int sampleLocation = (int)(Math.floor(screenP.x)) + app.width * ((int) Math.floor(screenP.y));
        return app.pixels[sampleLocation];
    }

    // Store samples as screen coordinates.
    @Override
    protected PVector2 toIntermediateRepresentation(PVector2 p) {
	return LayoutUtil.normalizedXyToScreen(p, app.width, app.height);
    }
    
    public void draw()
    {
        draw(app.millis() / 1000.);
    }

}
