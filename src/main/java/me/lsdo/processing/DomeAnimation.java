package me.lsdo.processing;

import java.util.*;

/**
 * Created by shen on 2016/06/28.
 *
 * THis forms the basis for a dome animation.
 */
public abstract class DomeAnimation<T extends LedPixel> {

    public static final double FRAMERATE_SMOOTHING_FACTOR = .9;  // [0, 1) -- higher == smoother
    
    public PixelMesh<? extends T> dome;
    
    private boolean initialized = false;
    private double lastT = 0;
    public double frameRate = 0.;  // fps

    InputControl ctrl;
    boolean txChanged = false;
    
    public DomeAnimation(PixelMesh<? extends T> dome) {
        this.dome = dome;
	initControl();
    }
    
    void initControl() {
        ctrl = new InputControl();
	ctrl.init();

        ctrl.registerHandler("jog_a", new InputControl.InputHandler() {
		@Override
                public void jog(boolean pressed) {
                    boolean forward = pressed;
		    dome.placement.xo += (forward ? 1 : -1) * .01;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("jog-xo", new InputControl.InputHandler() {
		@Override
                public void jog(boolean pressed) {
                    boolean forward = pressed;
		    dome.placement.xo += (forward ? 1 : -1) * .01;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("xo", new InputControl.InputHandler() {
		@Override
                public void set(double d) {
		    dome.placement.xo = d;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("jog_b", new InputControl.InputHandler() {
		@Override
                public void jog(boolean pressed) {
                    boolean forward = pressed;
		    dome.placement.yo += (forward ? 1 : -1) * .01;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("jog-yo", new InputControl.InputHandler() {
		@Override
                public void jog(boolean pressed) {
                    boolean forward = pressed;
		    dome.placement.yo += (forward ? 1 : -1) * .01;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("yo", new InputControl.InputHandler() {
		@Override
                public void set(double d) {
		    dome.placement.yo = d;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("pitch_a", new InputControl.InputHandler() {
		@Override
                public void slider(double val) {
		    dome.placement.rot = 2*Math.PI*(val - .5);
		    txChanged = true;
                }
            });
        ctrl.registerHandler("jog-rot", new InputControl.InputHandler() {
		@Override
                public void jog(boolean pressed) {
                    boolean forward = pressed;
		    dome.placement.rot += (forward ? 1 : -1) * .01 * Math.PI;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("rot", new InputControl.InputHandler() {
		@Override
                public void set(double d) {
		    dome.placement.rot = Math.toRadians(d);
		    txChanged = true;
                }
            });
        ctrl.registerHandler("pitch_b", new InputControl.InputHandler() {
		@Override
                public void slider(double val) {
		    dome.placement.scale = Math.exp(2*(val - .5));
		    txChanged = true;
                }
            });
        ctrl.registerHandler("jog-scale", new InputControl.InputHandler() {
		@Override
                public void jog(boolean pressed) {
                    boolean forward = pressed;
		    dome.placement.scale *= 1. + (forward ? 1 : -1) * .01;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("scale", new InputControl.InputHandler() {
		@Override
                public void set(double d) {
		    dome.placement.scale = d;
		    txChanged = true;
                }
            });
        ctrl.registerHandler("playpause_a", new InputControl.InputHandler() {
		@Override
                public void button(boolean pressed) {
                    if (pressed) {
			if (dome instanceof Prometheus) {
			    Prometheus p = (Prometheus)dome;
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
		    if (dome instanceof Prometheus) {
			Prometheus p = (Prometheus)dome;
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
		    if (!(dome instanceof Prometheus)) {
			return;
		    }
		    Prometheus prom = ((Prometheus)dome);
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
		    if (!(dome instanceof Prometheus)) {
			return;
		    }
		    Prometheus prom = ((Prometheus)dome);
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
		    if (!(dome instanceof Prometheus)) {
			return;
		    }
		    Prometheus prom = ((Prometheus)dome);
		    prom.setFlapAngle(prom.minFlapAngle * (1 - val) + prom.maxFlapAngle * val);
		    txChanged = true;
                }
            });
        ctrl.registerHandler("flap-angle", new InputControl.InputHandler() {
		@Override
                public void slider(double val) {
		    if (!(dome instanceof Prometheus)) {
			return;
		    }
		    Prometheus prom = ((Prometheus)dome);
		    prom.setFlapAngle(prom.minFlapAngle * (1 - val) + prom.maxFlapAngle * val);
		    txChanged = true;
                }
            });
        ctrl.registerHandler("load_a", new InputControl.InputHandler() {
		@Override
                public void button(boolean pressed) {
		    if (pressed && DomeAnimation.this instanceof WindowAnimation) {
			WindowAnimation win = (WindowAnimation)DomeAnimation.this;
			win.preserveAspect = !win.preserveAspect;
			txChanged = true;
		    }
                }
            });
        ctrl.registerHandler("stretch", new InputControl.InputHandler() {
		@Override
                public void set(boolean b) {
		    if (DomeAnimation.this instanceof WindowAnimation) {
			WindowAnimation win = (WindowAnimation)DomeAnimation.this;
			win.preserveAspect = !b;
			txChanged = true;
		    }
                }
            });
        ctrl.registerHandler("wingmode_unified", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			((Prometheus)dome).mode = WingDisplayMode.UNIFIED;
		    }
		    txChanged = true;
		}
            });
        ctrl.registerHandler("wingmode_mirror", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			((Prometheus)dome).mode = WingDisplayMode.MIRROR;
		    }
		    txChanged = true;
		}
            });
        ctrl.registerHandler("wingmode_flip", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			((Prometheus)dome).mode = WingDisplayMode.FLIP_HORIZ;
		    }
		    txChanged = true;
		}
            });
        ctrl.registerHandler("wingmode_rotate", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			((Prometheus)dome).mode = WingDisplayMode.ROTATE_180;
		    }
		    txChanged = true;
		}
            });
        ctrl.registerHandler("stretch_yes", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			if (DomeAnimation.this instanceof WindowAnimation) {
			    WindowAnimation win = (WindowAnimation)DomeAnimation.this;
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
			if (DomeAnimation.this instanceof WindowAnimation) {
			    WindowAnimation win = (WindowAnimation)DomeAnimation.this;
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
	if (dome instanceof Prometheus && ((Prometheus)dome).manageFlapState(this)) {
	    txChanged = true;
	}
	if (txChanged) {
	    if (this instanceof PixelTransform.TransformListener) {
		((PixelTransform.TransformListener)this).transformChanged();
	    }
	    txChanged = false;
	    /*
	    System.out.println("xo: " + dome.placement.xo);
	    System.out.println("yo: " + dome.placement.yo);
	    System.out.println("rot: " + dome.placement.rot);
	    System.out.println("scale: " + dome.placement.scale);
	    */
	}
	
	double deltaT = t - lastT;
	lastT = t;
	updateFramerate(deltaT);
	
        preFrame(t, deltaT);
        for (T c : dome.coords){
	    if (c.spacerPixel) {
		continue;
	    }
	    
            dome.setColor(c, drawPixel(c, t));
        }
        postFrame(t);
	// TODO: frame post-processing (global contrast adjustment, etc.?)
	dome.dispatch();
    }
    
    public PixelMesh<? extends T> getDome(){
        return dome;
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

    private void updateFramerate(double frameT) {
	double avgFrameLen = frameRate <= 0. ? frameT : 1. / frameRate;
        avgFrameLen = FRAMERATE_SMOOTHING_FACTOR * avgFrameLen + (1 - FRAMERATE_SMOOTHING_FACTOR) * frameT;
	frameRate = 1. / avgFrameLen;
    }
}
