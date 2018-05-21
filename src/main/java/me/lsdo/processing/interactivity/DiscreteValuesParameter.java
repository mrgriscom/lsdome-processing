package me.lsdo.processing.interactivity;

public abstract class DiscreteValuesParameter<T> extends Parameter<T> {

    public DiscreteValuesParameter(String name) {
	super(name);
    }
    
    public abstract T[] values();
    
    public void cycleNext() {
	T[] vals = values();
	for (int i = 0; i < vals.length; i++) {
	    if (get() == vals[i]) {
		set(vals[(i + 1) % vals.length]);
		break;
	    }
	}
    }
    
    public void setIx(int i) {
	set(enumByIx(i));
    }
    
    public void setName(String name) {
	set(enumByName(name));
    }

    public T enumByIx(int i) {
	return values()[i];
    }

    public T enumByName(String name) {
	name = name.toLowerCase();
	for (T val : values()) {
	    String valName = enumName(val).toLowerCase();
	    if (name.equals(valName)) {
		return val;
	    }
	}
	throw new IllegalArgumentException("invalid enum value for " + this.name + ": " + name);
    }
    
    public abstract String enumName(T val);
    public abstract String enumCaption(T val);

    public static class EnumDisplay {
	public String name;
	public String caption;

	public EnumDisplay(String name, String caption) {
	    this.name = name.toLowerCase();
	    this.caption = (caption != null ? caption : this.name);
	}
    }
    
    public EnumParameter.EnumDisplay[] getCaptions() {
	T[] vals = values();
	EnumDisplay[] disp = new EnumDisplay[vals.length];
	for (int i = 0; i < vals.length; i++) {
	    T e = vals[i];
	    disp[i] = new EnumDisplay(enumName(e), enumCaption(e));
	}
	return disp;
    }

    public void bindDirect(InputControl ctrl, String id) {
	ctrl.registerHandler(id, new InputControl.InputHandler() {
		@Override
		public void set(String val) {
		    try {
			setName(val);
		    } catch (IllegalArgumentException e) {
			// ignore; leave mode as is
			System.out.println(e.getMessage());
		    }
		}
	    });
    }

    public void bindPressToCycle(InputControl ctrl, String[] ids) {
	for (String id : ids) {
	    ctrl.registerHandler(id, new InputControl.InputHandler() {
		    @Override
		    public void button(boolean pressed) {
			if (pressed) {
			    cycleNext();
			}
		    }
		});
	}
    }

    public void bindRadioButtons(InputControl ctrl, String prefix) {
	for (T val : values()) {
	    final String valName = enumName(val).toLowerCase();
	    ctrl.registerHandler(prefix + "_" + valName, new InputControl.InputHandler() {
		    @Override
		    public void button(boolean pressed) {
			if (pressed) {
			    setName(valName);
			}
		    }
		});
	}
    }

    public void bindEnum(InputControl ctrl, String id) {
	bindDirect(ctrl, id);
	bindRadioButtons(ctrl, id);
    }

}
