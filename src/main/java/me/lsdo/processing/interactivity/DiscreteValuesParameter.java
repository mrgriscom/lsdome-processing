package me.lsdo.processing.interactivity;

public interface DiscreteValuesParameter<T> {

    public T[] values();
    public void cycleNext();
    public void setIx(int i);
    public void setName(String name);
    public T enumByIx(int i);
    public T enumByName(String name);
    public EnumParameter.EnumDisplay[] getCaptions();
	    
}
