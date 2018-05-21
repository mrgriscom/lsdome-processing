package me.lsdo.processing.interactivity;

// likely bindings:
// button press to toggle
// button instantaneous (pressed = true, released = false), + inverted
// radio button t/f (like 2-valued enum)

public class BooleanParameter extends DiscreteValuesParameter<Boolean> {

    // public settings -- after initialization, do not modify directly
    
    // optional, but use when treating like a 2-value enum
    public String trueCaption;
    public String falseCaption;

    public BooleanParameter(String name) {
	super(name);
    }
    
    // equivalent to cycleNext()
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

    public Boolean[] values() {
	return new Boolean[] {true, false};
    }
        
    public String enumName(Boolean val) {
	return (val ? "true" : "false");
    }

    public String enumCaption(Boolean val) {
	return (val ? trueCaption : falseCaption);
    }
    
}