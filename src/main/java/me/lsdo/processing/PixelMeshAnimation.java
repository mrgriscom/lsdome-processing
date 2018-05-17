package me.lsdo.processing;

import java.util.*;

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

    InputControl ctrl;
    boolean txChanged = false;
    
    public PixelMeshAnimation(PixelMesh<? extends T> mesh) {
        this.mesh = mesh;
	initControl();
    }
    
    void initControl() {
        ctrl = new InputControl();
	ctrl.init();

        ctrl.registerHandler("jog_a", new InputControl.InputHandler() {
		@Override
                public void jog(boolean pressed) {
                    boolean forward = pressed;
		    mesh.placement.xo += (forward ? 1 : -1) * .01;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("jog-xo", new InputControl.InputHandler() {
		@Override
                public void jog(boolean pressed) {
                    boolean forward = pressed;
		    mesh.placement.xo += (forward ? 1 : -1) * .01;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("xo", new InputControl.InputHandler() {
		@Override
                public void set(double d) {
		    mesh.placement.xo = d;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("jog_b", new InputControl.InputHandler() {
		@Override
                public void jog(boolean pressed) {
                    boolean forward = pressed;
		    mesh.placement.yo += (forward ? 1 : -1) * .01;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("jog-yo", new InputControl.InputHandler() {
		@Override
                public void jog(boolean pressed) {
                    boolean forward = pressed;
		    mesh.placement.yo += (forward ? 1 : -1) * .01;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("yo", new InputControl.InputHandler() {
		@Override
                public void set(double d) {
		    mesh.placement.yo = d;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("pitch_a", new InputControl.InputHandler() {
		@Override
                public void slider(double val) {
		    mesh.placement.rot = 2*Math.PI*(val - .5);
		    txChanged = true;
                }
            });
        ctrl.registerHandler("jog-rot", new InputControl.InputHandler() {
		@Override
                public void jog(boolean pressed) {
                    boolean forward = pressed;
		    mesh.placement.rot += (forward ? 1 : -1) * .01 * Math.PI;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("rot", new InputControl.InputHandler() {
		@Override
                public void set(double d) {
		    mesh.placement.rot = Math.toRadians(d);
		    txChanged = true;
                }
            });
        ctrl.registerHandler("pitch_b", new InputControl.InputHandler() {
		@Override
                public void slider(double val) {
		    mesh.placement.scale = Math.exp(2*(val - .5));
		    txChanged = true;
                }
            });
        ctrl.registerHandler("jog-scale", new InputControl.InputHandler() {
		@Override
                public void jog(boolean pressed) {
                    boolean forward = pressed;
		    mesh.placement.scale *= 1. + (forward ? 1 : -1) * .01;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("scale", new InputControl.InputHandler() {
		@Override
                public void set(double d) {
		    mesh.placement.scale = d;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("playpause_a", new InputControl.InputHandler() {
		@Override
                public void button(boolean pressed) {
                    if (pressed) {
			if (mesh instanceof Prometheus) {
			    Prometheus p = (Prometheus)mesh;
			    WingDisplayMode[] modes = WingDisplayMode.values();
			    for (int i = 0; i < modes.length; i++) {
				if (p.mode == modes[i]) {
				    p.mode = modes[(i + 1) % modes.length];
				    System.out.println("setting wing mode " + p.mode.name());
				    break;
				}
			    }
			    txChanged = true;
			}
                    }
                }
            });
        ctrl.registerHandler("wingmode", new InputControl.InputHandler() {
		@Override
                public void set(String s) {
		    if (mesh instanceof Prometheus) {
			Prometheus p = (Prometheus)mesh;
			if (s.equals("unified")) {
			    p.mode = WingDisplayMode.UNIFIED;
			} else if (s.equals("mirror")) {
			    p.mode = WingDisplayMode.MIRROR;
			} else if (s.equals("flip")) {
			    p.mode = WingDisplayMode.FLIP_HORIZ;
			} else if (s.equals("opposite")) {
			    p.mode = WingDisplayMode.ROTATE_180;
			} else {
			    return;
			}
			txChanged = true;
                    }
                }
            });
        ctrl.registerHandler("playpause_b", new InputControl.InputHandler() {
		@Override
                public void button(boolean pressed) {
		    if (!(mesh instanceof Prometheus)) {
			return;
		    }
		    Prometheus prom = ((Prometheus)mesh);
		    if (pressed) {
			prom.startFlapping();
		    } else {
			prom.stopFlapping();
		    }
		    txChanged = true;
                }
            });
        ctrl.registerHandler("flap", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (!(mesh instanceof Prometheus)) {
			return;
		    }
		    Prometheus prom = ((Prometheus)mesh);
		    if (pressed) {
			prom.startFlapping();
		    } else {
			prom.stopFlapping();
		    }
		    txChanged = true;
                }
            });
        ctrl.registerHandler("mixer", new InputControl.InputHandler() {
		@Override
                public void slider(double val) {
		    if (!(mesh instanceof Prometheus)) {
			return;
		    }
		    Prometheus prom = ((Prometheus)mesh);
		    prom.setFlapAngle(prom.minFlapAngle * (1 - val) + prom.maxFlapAngle * val);
		    txChanged = true;
                }
            });
        ctrl.registerHandler("flap-angle", new InputControl.InputHandler() {
		@Override
                public void slider(double val) {
		    if (!(mesh instanceof Prometheus)) {
			return;
		    }
		    Prometheus prom = ((Prometheus)mesh);
		    prom.setFlapAngle(prom.minFlapAngle * (1 - val) + prom.maxFlapAngle * val);
		    txChanged = true;
                }
            });
        ctrl.registerHandler("flap-depth", new InputControl.InputHandler() {
		@Override
                public void slider(double val) {
		    if (!(mesh instanceof Prometheus)) {
			return;
		    }
		    Prometheus prom = ((Prometheus)mesh);
		    prom.maxFlap = prom.minMaxFlap * (1 - val) + prom.maxMaxFlap * val;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("flap-speed", new InputControl.InputHandler() {
		@Override
                public void slider(double val) {
		    if (!(mesh instanceof Prometheus)) {
			return;
		    }
		    Prometheus prom = ((Prometheus)mesh);
		    prom.flapPeriod = prom.maxFlapPeriod * Math.pow(prom.minFlapPeriod / prom.maxFlapPeriod, val);
		    txChanged = true;
                }
            });
        ctrl.registerHandler("load_a", new InputControl.InputHandler() {
		@Override
                public void button(boolean pressed) {
		    if (pressed && PixelMeshAnimation.this instanceof WindowAnimation) {
			WindowAnimation win = (WindowAnimation)PixelMeshAnimation.this;
			win.preserveAspect = !win.preserveAspect;
			txChanged = true;
		    }
                }
            });
        ctrl.registerHandler("stretch", new InputControl.InputHandler() {
		@Override
                public void set(boolean b) {
		    if (PixelMeshAnimation.this instanceof WindowAnimation) {
			WindowAnimation win = (WindowAnimation)PixelMeshAnimation.this;
			win.preserveAspect = !b;
			txChanged = true;
		    }
                }
            });
        ctrl.registerHandler("wingmode_unified", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			((Prometheus)mesh).mode = WingDisplayMode.UNIFIED;
		    }
		    txChanged = true;
		}
            });
        ctrl.registerHandler("wingmode_mirror", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			((Prometheus)mesh).mode = WingDisplayMode.MIRROR;
		    }
		    txChanged = true;
		}
            });
        ctrl.registerHandler("wingmode_flip", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			((Prometheus)mesh).mode = WingDisplayMode.FLIP_HORIZ;
		    }
		    txChanged = true;
		}
            });
        ctrl.registerHandler("wingmode_rotate", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			((Prometheus)mesh).mode = WingDisplayMode.ROTATE_180;
		    }
		    txChanged = true;
		}
            });
        ctrl.registerHandler("stretch_yes", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			if (PixelMeshAnimation.this instanceof WindowAnimation) {
			    WindowAnimation win = (WindowAnimation)PixelMeshAnimation.this;
			    win.preserveAspect = false;
			    txChanged = true;
			}
		    }
		}
            });
        ctrl.registerHandler("stretch_no", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			if (PixelMeshAnimation.this instanceof WindowAnimation) {
			    WindowAnimation win = (WindowAnimation)PixelMeshAnimation.this;
			    win.preserveAspect = true;
			    txChanged = true;
			}
		    }
		}
            });
    }
    
    public void draw(double t) {
	if (!initialized) {
	    init();
	    initialized = true;
	}

	ctrl.processInput();
	// hacky
	if (mesh instanceof Prometheus && ((Prometheus)mesh).manageFlapState(this)) {
	    txChanged = true;
	}
	if (txChanged) {
	    if (this instanceof PixelTransform.TransformListener) {
		((PixelTransform.TransformListener)this).transformChanged();
	    }
	    txChanged = false;
	    /*
	    System.out.println("xo: " + mesh.placement.xo);
	    System.out.println("yo: " + mesh.placement.yo);
	    System.out.println("rot: " + mesh.placement.rot);
	    System.out.println("scale: " + mesh.placement.scale);
	    */
	}
	
	double deltaT = t - lastT;
	lastT = t;
	updateFramerate(deltaT);
	
        preFrame(t, deltaT);
        for (T c : mesh.coords){
	    if (c.spacerPixel) {
		continue;
	    }
	    
            mesh.setColor(c, drawPixel(c, t));
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
                Thread.sleep((int)(1000. * delay));
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
