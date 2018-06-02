package me.lsdo.processing.interactivity;

import java.util.*;

public abstract class Parameter<T> {

    static List<Parameter> parameters = new ArrayList<Parameter>();
    
    // public settings -- after initialization, do not modify directly
    
    public boolean verbose = false;
    
    // internal vars

    public String name;
    public String category;
    T value;
    boolean isSet = false;
    T defaultValue;

    public Parameter(String name, String category) {
	this.name = name;
	this.category = category;
	parameters.add(this);
    }
    
    public void init(T initValue) {
	this.defaultValue = initValue;
	this.reset();
    }

    public void set(T value) {
	value = constrainValue(value);
	if (this.value != value || !isSet) {
	    T prev = (isSet ? this.value : null);
	    this.value = value;
	    isSet = true;
	    
	    if (verbose) {
		System.out.println(name + ": " + value);
	    }

	    onSet();
	    if (prev != null) {
		onChange(prev);
	    }
	}
    }

    public T get() {
	if (!isSet) {
	    throw new IllegalStateException("parameter " + name + " has not been initialized");
	}
	return value;
    }
    
    public void reset() {
	set(defaultValue);
    }

    // called whenever the value changes or when the parameter is first set
    public void onSet() {}
    // called only when the value changes from a previous value
    public void onChange(T prev) {}
    
    public T constrainValue(T value) {
	return value;
    }

    public void bind(InputControl ctrl) {
	ctrl.registerHandler(name, getHandler());
    }

    public abstract InputControl.InputHandler getHandler();

    public InputControl.ParameterJson toJson() {
	InputControl.ParameterJson json = new InputControl.ParameterJson();
	json.name = name;
	json.category = category;
	return json;
    }
    
}
