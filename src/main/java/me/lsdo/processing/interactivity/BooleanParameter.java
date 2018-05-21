package me.lsdo.processing.interactivity;

// likely bindings:
// button press to toggle
// button instantaneous (pressed = true, released = false), + inverted
// radio button t/f (like 2-valued enum)

public class BooleanParameter extends Parameter<Boolean> implements DiscreteValuesParameter<Boolean> {

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

    
    // enum mimicry
    
    public Boolean[] values() {
	return new Boolean[] {true, false};
    }
    
    public void cycleNext() {
	toggle();
    }
    
    public void setIx(int i) {
	set(enumByIx(i));
    }
    
    public void setName(String name) {
	set(enumByName(name));
    }
	
    public Boolean enumByIx(int i) {
	return values()[i];
    }
    
    public Boolean enumByName(String name) {
	if (name.equals("true")) {
	    return true;
	} else if (name.equals("false")) {
	    return false;
	} else {
	    throw new IllegalArgumentException(this.name + " must be true or false; got " + name);
	}
    }

    public EnumParameter.EnumDisplay[] getCaptions() {
	return new EnumParameter.EnumDisplay[] {
	    new EnumParameter.EnumDisplay("true", trueCaption),
	    new EnumParameter.EnumDisplay("false", falseCaption)
	};
    }
    
}
