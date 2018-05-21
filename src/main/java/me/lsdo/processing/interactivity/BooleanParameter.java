package me.lsdo.processing.interactivity;

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
	return (val ? "yes" : "no");
    }

    public String enumCaption(Boolean val) {
	return (val ? trueCaption : falseCaption);
    }

    // equivalent to bindPressToCycle()
    public void bindPressToToggle(InputControl ctrl, String[] ids) {
	for (String id : ids) {
	    ctrl.registerHandler(id, new InputControl.InputHandler() {
		    @Override
		    public void button(boolean pressed) {
			if (pressed) {
			    toggle();
			}
		    }
		});
	}
    }

    public void bindAction(InputControl ctrl, String[] ids) {
	bindTrueWhilePressed(ctrl, ids);
    }
    public void bindTrueWhilePressed(InputControl ctrl, String[] ids) {
	bindMirrorPressState(ctrl, false, ids);
    }
    public void bindFalseWhilePressed(InputControl ctrl, String[] ids) {
	bindMirrorPressState(ctrl, true, ids);
    }
    private void bindMirrorPressState(InputControl ctrl, final boolean falseWhilePressed, String[] ids) {
	for (String id : ids) {
	    ctrl.registerHandler(id, new InputControl.InputHandler() {
		    @Override
		    public void button(boolean pressed) {
			BooleanParameter.this.set(falseWhilePressed ^ pressed);
		    }
		});
	}
    }
    
}
