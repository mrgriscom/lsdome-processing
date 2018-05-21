package me.lsdo.processing.interactivity;

// likely bindings:
// radio buttons
// cycle to next

public class EnumParameter<T extends Enum<T>> extends Parameter<T> {

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
    
}
