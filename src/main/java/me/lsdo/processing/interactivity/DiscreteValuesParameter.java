package me.lsdo.processing.interactivity;

public abstract class DiscreteValuesParameter<T> extends Parameter<T> {

    public DiscreteValuesParameter(String name, String category) {
	super(name, category);
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
	    String valName = normalizedEnumName(val);
	    if (name.equals(valName)) {
		return val;
	    }
	}
	throw new IllegalArgumentException("invalid enum value for " + this.name + ": " + name);
    }

    public String normalizedEnumName(T val) {
	return enumName(val).toLowerCase();
    }
    
    public abstract String enumName(T val);
    public abstract String enumCaption(T val);

    public InputControl.InputHandler getHandler() {
	return new InputControl.InputHandler() {
	    @Override
	    public void set(String val) {
		try {
		    setName(val);
		} catch (IllegalArgumentException e) {
		    // ignore; leave mode as is
		    System.out.println(e.getMessage());
		}
	    }
	    
	    @Override
	    public void button(boolean pressed) {
		if (pressed) {
		    cycleNext();
		}
	    }
	};
    }

    public InputControl.ParameterJson toJson() {
	InputControl.ParameterJson json = super.toJson();
	json.isEnum = true;

	T[] vals = values();
	json.values = new String[vals.length];
	json.captions = new String[vals.length];
	for (int i = 0; i < vals.length; i++) {
	    T e = vals[i];
	    String name = normalizedEnumName(e);
	    String caption = enumCaption(e);
	    json.values[i] = name;
	    json.captions[i] = (caption != null ? caption : name);
	}
	return json;
    }

}
