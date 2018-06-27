package me.lsdo.processing.interactivity;

import java.util.*;

// EnumParameter whose values are created at runtime -- not bound to a static java enum

public class DynamicEnumParameter<T> extends DiscreteValuesParameter<T> {

    T[] vals;
    Map<T, String> captions;

    public DynamicEnumParameter(String name, String category, T[] vals) {
	this(name, category, vals, null, null);
    }
    
    public DynamicEnumParameter(String name, String category, T[] vals, String[] captions) {
	this(name, category, vals, captions, null);
    }

    public DynamicEnumParameter(String name, String category, T[] vals, Map<T, String> captions) {
	this(name, category, vals, null, captions);
    }
    
    private DynamicEnumParameter(String name, String category, T[] vals, String[] listCaptions, Map<T, String> mapCaptions) {
	super(name, category);
	this.vals = vals;

	if (listCaptions != null) {
	    this.captions = captionsMap(vals, listCaptions);
	}
	if (mapCaptions != null) {
	    this.captions = mapCaptions;
	}
    }

    private Map<T, String> captionsMap(T[] vals, String[] captions) {
	if (vals.length != captions.length) {
	    throw new IllegalArgumentException("captions array is mis-sized");
	}
	Map<T, String> map = new HashMap<T, String>();
	for (int i = 0; i < vals.length; i++) {
	    String caption = captions[i];
	    if (caption != null) {
		map.put(vals[i], caption);
	    }
	}
	return map;
    }

    public T[] values() {
	return vals;
    }
    
    public String enumName(T val) {
	return val.toString();
    }

    public String enumCaption(T val) {
	return captions.get(val);
    }

}
