package me.lsdo.processing.interactivity;

// likely bindings:
// radio buttons
// cycle to next

public class EnumParameter<T extends Enum<T>> extends Parameter<T> implements DiscreteValuesParameter<T> {

    public static interface CaptionedEnum {
	String caption();
    }
    
    public static class EnumDisplay {
	public String name;
	public String caption;

	public EnumDisplay(String name, String caption) {
	    this.name = name.toLowerCase();
	    this.caption = (caption != null ? caption : this.name);
	}
    }
    
    // captions in enum def

    Class<T> enumClass;
    
    public EnumParameter(String name, Class<T> enumClass) {
	super(name);
	this.enumClass = enumClass;
    }

    public T[] values() {
	return enumClass.getEnumConstants();
    }
    
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
	name = name.toUpperCase();
	T[] vals = values();
	for (int i = 0; i < vals.length; i++) {
	    if (name.equals(vals[i].name())) {
		return vals[i];
	    }
	}
	throw new IllegalArgumentException("invalid enum value for " + this.name + ": " + name);
    }

    public EnumDisplay[] getCaptions() {
	T[] vals = values();
	EnumDisplay[] disp = new EnumDisplay[vals.length];
	for (int i = 0; i < vals.length; i++) {
	    T e = vals[i];
	    disp[i] = new EnumDisplay(e.name(), e instanceof CaptionedEnum ? ((CaptionedEnum)e).caption() : null);
	}
	return disp;
    }
    
}
