package me.lsdo.processing.geometry.prometheus;

import me.lsdo.processing.InputControl;
import me.lsdo.processing.LedPixel;
import me.lsdo.processing.PixelMeshAnimation;
import me.lsdo.processing.PixelTransform;
import me.lsdo.processing.util.*;

public class FlapManager {

    Prometheus mesh;
    
    // flapping parameter limits
    public double maxFlapDepth = .01;
    public double minFlapDepth = .5;
    public double minFlapAngle = Math.toRadians(-10);
    public double maxFlapAngle = Math.toRadians(20);
    public double minFlapPeriod = .25;
    public double maxFlapPeriod = 2;

    // flapping parameters
    public double flapPeriod = .5; // seconds
    public double flapDepth = maxFlapDepth; // percentage
    // set via setFlapAngle()
    public double flapAngle = 0; // radians
    // generally not expected to be modified
    public double flapVanishingPointOffset = .25; // meters
    
    // flapping instantaneous state variables
    double flappingStart = -1; // timestamp
    double flappingEnd = -1; // timestamp
    double flapLevel = 1.; // percentage
    double flapOrigin; // meters
    boolean isFlapping = false;

    public FlapManager(Prometheus mesh) {
	this.mesh = mesh;
	setFlapAngle(this.flapAngle);
    }
    
    public void setFlapAngle(double flapAngle) {
	this.flapAngle = flapAngle;

	double min = Double.POSITIVE_INFINITY;
	for (LedPixel px : mesh.coords) {
	    if (px.spacerPixel) {
		continue;
	    }
	    
	    double x = LayoutUtil.Vrot(px.toXY(), flapAngle).x;
	    min = Math.min(min, x);
	}
	flapOrigin = min - flapVanishingPointOffset;
    }
    
    public void startFlapping() {
	if (!flappingActive()) {
	    flappingStart = Config.clock();
	}
	flappingEnd = -1;
    }

    public void stopFlapping() {
	flappingEnd = flappingStart + flapPeriod * Math.ceil((Config.clock() - flappingStart) / flapPeriod);
    }

    boolean flappingActive() {
	return (flappingStart >= 0 && (flappingEnd < 0 || Config.clock() < flappingEnd));
    }

    double flapEasing(double x) {
	return MathUtil.sineEasing(x);
    }

    PixelTransform getFlapTransform() {
	return new PixelTransform() {
	    public PVector2 transform(PVector2 p) {
		if (flapLevel == 1.) {
		    return p;
		} else {
		    p = LayoutUtil.Vrot(p, flapAngle);
		    p = LayoutUtil.V((p.x - flapOrigin) / Math.max(flapLevel, .01) + flapOrigin, p.y);
		    p = LayoutUtil.Vrot(p, -flapAngle);
		    return p;
		}
	    }
	};
    }

    // return if the transform has changed
    public boolean manageState(PixelMeshAnimation anim) {
	boolean active = flappingActive();
	if (active) {
	    double flapProgress = ((Config.clock() - flappingStart) / flapPeriod) % 1.; // 0 to 1
	    double easingX = 1 - Math.abs(2*flapProgress - 1); // 0 to 1 to 0
	    flapLevel = 1 - flapEasing(easingX); // 1 to 0 to 1
	    flapLevel = flapDepth + flapLevel * (1 - flapDepth);
	} else {
	    flapLevel = 1.;
	}
	if (anim instanceof PixelTransform.TransformListener) {
	    ((PixelTransform.TransformListener)anim).transformAnimating(active);
	}
	boolean changed = (active != isFlapping);
	isFlapping = active;
	return active || changed;
    }

    public void registerHandlers(InputControl ctrl) {
	ctrl.registerHandler("playpause_b", new InputControl.InputHandler() {
		@Override
                public void button(boolean pressed) {
		    if (pressed) {
			startFlapping();
		    } else {
			stopFlapping();
		    }
                }
            });
        ctrl.registerHandler("flap", new InputControl.InputHandler() {
		@Override
                public void set(boolean pressed) {
		    if (pressed) {
			startFlapping();
		    } else {
			stopFlapping();
		    }
                }
            });
        ctrl.registerHandler("mixer", new InputControl.InputHandler() {
		@Override
                public void slider(double val) {
		    setFlapAngle(minFlapAngle * (1 - val) + maxFlapAngle * val);
                }
            });
        ctrl.registerHandler("flap-angle", new InputControl.InputHandler() {
		@Override
                public void slider(double val) {
		    setFlapAngle(minFlapAngle * (1 - val) + maxFlapAngle * val);
                }
            });
        ctrl.registerHandler("flap-depth", new InputControl.InputHandler() {
		@Override
                public void slider(double val) {
		    flapDepth = minFlapDepth * (1 - val) + maxFlapDepth * val;
                }
            });
        ctrl.registerHandler("flap-speed", new InputControl.InputHandler() {
		@Override
                public void slider(double val) {
		    flapPeriod = maxFlapPeriod * Math.pow(minFlapPeriod / maxFlapPeriod, val);
                }
            });
    }

}
