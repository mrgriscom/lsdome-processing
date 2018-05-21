package me.lsdo.processing.interactivity;

// TODO builder pattern?

public class NumericParameter {

    public enum Scale {
	LINEAR,
	LOG,
    };

    // public settings -- after initialization, do not modify directly
    
    // optional, but required for slider control
    // max doesn't have to be greater than min -- think more like left and right bounds of a slider
    public double min, max;
    // if true, allow setting values outside of bounded range, even if min/max set
    public boolean softLimits = false;
    public Scale scale = Scale.LINEAR;
    public boolean verbose = false;

    // internal vars

    public String name;
    // sensitivity is the increment for one 'step', which should be thought of as ~1/60th of a jog wheel rotation
    // use setSensitivity()
    public double sensitivity;
    double value = Double.NaN;
    double defaultValue;
    double logMin, logMax;

    public NumericParameter(String name) {
	this.name = name;
    }
    
    public void init(double initValue) {
	if (hasBounds() && scale == Scale.LOG) {
	    if (min <= 0 || max <= 0) {
		throw new IllegalStateException("log scale must have positive bounds");
	    }
	    logMin = Math.log(min);
	    logMax = Math.log(max);
	}
	
	this.defaultValue = initValue;
	this.reset();
    }

    public void set(double value) {
	double cur = this.value;
	value = constrainValue(value);
	if (cur != value) {
	    if (verbose) {
		System.out.println(name + ": " + value);
	    }
	    this.value = value;
	    onChange(Double.isNaN(cur) ? value : cur);
	}
    }

    public double get() {
	return value;
    }

    public double getInternal() {
	return toInternal(value);
    }
    
    public void setSlider(double frac) {
	if (!hasBounds()) {
	    throw new IllegalStateException("bounds not set");
	}
	if (frac < 0. || frac > 1.) {
	    throw new IllegalArgumentException("frac must be in interval [0,1]");
	}
	if (scale == Scale.LINEAR) {
	    set(min * (1 - frac) + max * frac);
	} else if (scale == Scale.LOG) {
	    set(Math.exp(logMin * (1 - frac) + logMax * frac));
	}
    }

    public void increment(int numSteps) {
	for (int i = 0; i < Math.abs(numSteps); i++) {
	    step(numSteps > 0);
	}
    }

    public void step(boolean incr) {
	if (sensitivity == 0) {
	    throw new IllegalStateException("sensitivity not set");
	}
	if (scale == Scale.LINEAR) {
	    stepLinear(incr, sensitivity);
	} else if (scale == Scale.LOG) {
	    stepLog(incr, sensitivity);
	}
    }

    public void stepLinear(boolean incr, double sensitivity) {
	set(value + (incr ? 1 : -1) * sensitivity);
    }

    public void stepLog(boolean incr, double sensitivity) {
	double mult = 1 + sensitivity;
	set(value * (incr ? mult : 1./mult));
    }
    
    public void reset() {
	set(defaultValue);
    }

    public void onChange(double prev) { }

    public double toInternal(double value) {
	return value;
    }
    
    public boolean hasBounds() {
	return min != max;
    }

    double constrainValue(double value) {
	if (hasBounds() && !softLimits) {
	    double upperBound = Math.max(min, max);
	    double lowerBound = Math.min(min, max);
	    return Math.min(Math.max(value, lowerBound), upperBound);
	} else {
	    return value;
	}
    }

    public void setSensitivity(double sensitivity) {
	this.sensitivity = sensitivity;
    }
    
}
