package me.lsdo.processing;

import java.util.*;
import me.lsdo.processing.geometry.prometheus.*;
import me.lsdo.processing.interactivity.*;
import me.lsdo.processing.util.*;

/**
 * Superclass of all animation classes: takes a geometry, invokes a function to get the
 * color of each pixel for each frame, manages framerate, etc.
 */
public abstract class PixelMeshAnimation<T extends LedPixel> {

    public static final double FRAMERATE_SMOOTHING_FACTOR = .9;  // [0, 1) -- higher == smoother

    public PixelMesh<? extends T> mesh;

    private boolean initialized = false;
    private double lastT = 0;
    public double frameRate = 0.;  // fps

    public InputControl ctrl;
    public NumericParameter globalBrightness;
    public NumericParameter brightness;
    public NumericParameter contrast;

    public PixelMeshAnimation(PixelMesh<? extends T> mesh) {
        this.mesh = mesh;

        ctrl = new InputControl();
        ctrl.init();
        ctrl.broadcastPixels(mesh);

        globalBrightness = new NumericParameter("luminance", "display");
        globalBrightness.min = 0.;
        globalBrightness.max = 1.;
        globalBrightness.init(Config.getSketchProperty("max_brightness", 1.));

        contrast = new NumericParameter("contrast", "display");
        contrast.min = 0.;
        contrast.max = 1.;
        contrast.init(Config.getSketchProperty("contrast", .5));

        brightness = new NumericParameter("contrast ref", "display");
        brightness.min = 0.;
        brightness.max = 1.;
        brightness.init(Config.getSketchProperty("brightness", .5));
    }

    public void draw(double t) {
	if (!initialized) {
	    init();
	    ctrl.finalizeParams();
	    initialized = true;
	}

	double deltaT = t - lastT;
	lastT = t;
	updateFramerate(deltaT);

	ctrl.processInput();
	mesh.beforeDraw(this);

        preFrame(t, deltaT);
        for (T c : mesh.coords()){
            mesh.setColor(c, OpcColor.scaleBrightness(OpcColor.brightnessContrast(drawPixel(c, t), brightness.get(), contrast.get()), globalBrightness.get()));
        }
        postFrame(t);
	// TODO: frame post-processing (global contrast adjustment, etc.?)
	mesh.dispatch();
    }

    // Main method that need to be implemented.
    protected abstract int drawPixel(T c, double t);

    /** Override this for pre-draw stuff.
     *  e.g. loadPixel, or advance animation state.
     * @param t time in seconds since start.
     * @param deltaT time in seconds since last frame.
     */
    protected void preFrame(double t, double deltaT){
    }

    /** Override this for post-draw stuff
     * e.g. save pixels.
     * @param t time in seconds since start
     */
    protected void postFrame(double t){
    }

    // Override: optional
    // Perform one-time initialization that for whatever reason can't be performed in the constructor
    protected void init() {}

    // Run the animation
    // note: never returns!
    public void run(float maxFPS) {
	while (true) {
            double start = Config.clock();
            draw(start);
            double end = Config.clock();
            double elapsed = end - start;
	    double delay = Math.max(1./maxFPS - elapsed, 0);
            try {
                Thread.sleep((int)Math.round(1000. * delay));
            } catch (InterruptedException ie) {
            }
        }
    }

    private void updateFramerate(double frameT) {
	double avgFrameLen = frameRate <= 0. ? frameT : 1. / frameRate;
        avgFrameLen = FRAMERATE_SMOOTHING_FACTOR * avgFrameLen + (1 - FRAMERATE_SMOOTHING_FACTOR) * frameT;
	frameRate = 1. / avgFrameLen;
    }
}
