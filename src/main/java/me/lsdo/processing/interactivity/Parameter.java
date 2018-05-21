package me.lsdo.processing.interactivity;

public class Parameter<T> {

    // public settings -- after initialization, do not modify directly
    
    public boolean verbose = false;
    
    // internal vars

    public String name;
    T value;
    boolean isSet = false;
    T defaultValue;

    public Parameter(String name) {
	this.name = name;
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
    
}
