package me.lsdo.processing.interactivity;

public class BooleanParameter extends Parameter<Boolean> {

    // public settings -- after initialization, do not modify directly
    
    // optional, but use when treating like a 2-value enum
    public String trueCaption;
    public String falseCaption;

    public BooleanParameter(String name) {
	super(name);
    }
    
    public void toggle() {
	set(!get());
    }
    
    public void onSet() {
	if (value) {
	    onTrue();
	} else {
	    onFalse();
	}
    }

    public void onTrue() {}
    public void onFalse() {}
    
}
