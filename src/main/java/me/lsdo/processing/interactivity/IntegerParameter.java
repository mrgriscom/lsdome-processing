package me.lsdo.processing.interactivity;

public class IntegerParameter extends NumericParameter {

    public IntegerParameter(String name) {
	super(name);
	setSensitivity(1);
    }
    
    public double toInternal(double value) {
	return Math.round(value);
    }
    
}
