package me.lsdo.processing.interactivity;

public class EnumParameter<T extends Enum<T>> extends DiscreteValuesParameter<T> {

    public static interface CaptionedEnum {
	String caption();
    }
    
    Class<T> enumClass;
    
    public EnumParameter(String name, String category, Class<T> enumClass) {
	super(name, category);
	this.enumClass = enumClass;
    }

    public T[] values() {
	return enumClass.getEnumConstants();
    }
    
    public String enumName(T val) {
	return val.name();
    }

    public String enumCaption(T val) {
	if (val instanceof CaptionedEnum) {
	    return ((CaptionedEnum)val).caption();
	} else {
	    return null;
	}
    }

}
