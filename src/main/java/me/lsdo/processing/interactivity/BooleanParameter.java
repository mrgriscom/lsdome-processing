package me.lsdo.processing.interactivity;

public class BooleanParameter extends DiscreteValuesParameter<Boolean> {

    // public settings -- after initialization, do not modify directly
    
    // optional, but use when treating like a 2-value enum
    public String trueCaption;
    public String falseCaption;

    public BooleanParameter(String name, String category) {
	super(name, category);
    }
    
    // equivalent to cycleNext()
    public void toggle() {
	set(!get());
    }

    // simulate a momentary press
    public void trigger() {
	set(true);
	set(false);
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
	return (val ? "yes" : "no");
    }

    public String enumCaption(Boolean val) {
	return (val ? trueCaption : falseCaption);
    }

    public static enum Affinity {
	STATE,
	ACTION,
    }
    public Affinity affinity = Affinity.STATE;
    public boolean invertPress = false;

    public InputControl.InputHandler getHandler() {
	final InputControl.InputHandler enumHandler = super.getHandler();
	return new InputControl.InputHandler() {
	    @Override
	    public void set(String val) {
		enumHandler.set(val);
	    }
	    
	    @Override
	    public void button(boolean pressed) {
		boolean falseWhilePressed = (affinity == Affinity.STATE && invertPress);
		BooleanParameter.this.set(falseWhilePressed ^ pressed);
	    }

	    @Override
	    public boolean customType(String type, String value) {
		if (type.equals("toggle")) {
		    enumHandler.button(true);
		    return true;
		}
		return false;
	    }
	};
    }
    
    public InputControl.ParameterJson toJson() {
	InputControl.ParameterJson json = super.toJson();
	if (affinity == Affinity.ACTION) {
	    json.isAction = true;
	}
	return json;
    }
}
