package me.lsdo.processing.geometry.prometheus;

import me.lsdo.processing.LedPixel;
import me.lsdo.processing.PixelMeshAnimation;
import me.lsdo.processing.PixelTransform;
import me.lsdo.processing.interactivity.*;
import me.lsdo.processing.util.*;

public class FlapManager {

    Prometheus mesh;

    // flapping parameters
    public NumericParameter flapPeriod; // seconds
    public NumericParameter flapDepth; // percentage
    public NumericParameter.Angle flapAngle; // degrees
    // generally not expected to be modified
    public double flapVanishingPointOffset = .25; // meters

    // flapping instantaneous state variables
    double flappingStart = -1; // timestamp
    double flappingEnd = -1; // timestamp
    double flapLevel = 1.; // percentage
    double flapOrigin; // meters
    boolean isFlapping = false;

    BooleanParameter flapAction;

    public FlapManager(final Prometheus mesh) {
	this.mesh = mesh;

	flapAction = new BooleanParameter("flap", "mesh_effects") {
		@Override
		public void onTrue() {
		    startFlapping();
		}

		@Override
		public void onFalse() {
		    stopFlapping();
		}
	    };
	flapAction.affinity = BooleanParameter.Affinity.MOMENTARY;
	flapAction.description = "FLAP";
	flapAction.verbose = true;

	flapPeriod = new NumericParameter("flap period", "mesh_effects");
	flapPeriod.verbose = true;
	flapPeriod.min = 2.;
	flapPeriod.max = .25;
	flapPeriod.scale = NumericParameter.Scale.LOG;

	flapDepth = new NumericParameter("flap depth", "mesh_effects");
	flapDepth.verbose = true;
	flapDepth.min = .5;
	flapDepth.max = .01;

	flapAngle = new NumericParameter.Angle("flap angle", "mesh_effects") {
		@Override
		public void onSet() {
		    double angle = getInternal();

		    double min = Double.POSITIVE_INFINITY;
		    for (LedPixel px : mesh.coords()) {
			double x = LayoutUtil.Vrot(px.toXY(), angle).x;
			min = Math.min(min, x);
		    }
		    flapOrigin = min - flapVanishingPointOffset;
		}
    	    };
	flapAngle.verbose = true;
	flapAngle.setLimits(-10, 20);

	flapPeriod.init(.5);
	flapDepth.init(flapDepth.max);
	flapAngle.init(0);
	flapAction.init(false);
    }

    public void startFlapping() {
	if (!flappingActive()) {
	    flappingStart = Config.clock();
	}
	flappingEnd = -1;
    }

    public void stopFlapping() {
	flappingEnd = flappingStart + flapPeriod.get() * Math.ceil((Config.clock() - flappingStart) / flapPeriod.get());
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
		    p = LayoutUtil.Vrot(p, flapAngle.getInternal());
		    p = LayoutUtil.V((p.x - flapOrigin) / Math.max(flapLevel, .01) + flapOrigin, p.y);
		    p = LayoutUtil.Vrot(p, -flapAngle.getInternal());
		    return p;
		}
	    }
	};
    }

    // return if the transform has changed
    public boolean manageState(PixelMeshAnimation anim) {
	boolean active = flappingActive();
	if (active) {
	    double flapProgress = ((Config.clock() - flappingStart) / flapPeriod.get()) % 1.; // 0 to 1
	    double easingX = 1 - Math.abs(2*flapProgress - 1); // 0 to 1 to 0
	    flapLevel = 1 - flapEasing(easingX); // 1 to 0 to 1
	    flapLevel = flapDepth.get() + flapLevel * (1 - flapDepth.get());
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

}
