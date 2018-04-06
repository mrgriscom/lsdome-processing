
package me.lsdo.processing;

import java.util.*;
//import processing.core.PVector;

public abstract class PixelMesh<T extends LedPixel> {

    List<OPC> opcs;
    private int[][] opcBuffers;

    // Positions of all the pixels in triangular grid coordinates (and in the order seen by
    // the fadecandy).
    public ArrayList<T> coords;

    // Mapping of pixel grid coordinates to xy locations (world coordinates, not screen
    // coordinates!)
    protected HashMap<LedPixel, PVector2> points;

    // Color
    protected HashMap<LedPixel, Integer> colors;

    public PixelMesh() {
	opcs = new ArrayList<OPC>();
	coords = new ArrayList<T>();
	points = new HashMap<LedPixel, PVector2>();
	colors = new HashMap<LedPixel, Integer>();
    }

    public void init() {
        for (LedPixel c : coords) {
            setColor(c, 0);
	}	
	initOpcBuffers();
    }
    
    public Integer getColor(LedPixel dCoord){
      return colors.get(dCoord);
    }

    public void setColor(LedPixel dCoord, Integer color){
      colors.put(dCoord, color);
    }

    public PVector2 getLocation(LedPixel dCoord){
        return points.get(dCoord);
    }

    public int getNumPoints(){
        return points.size();
    }

    // TODO figure out fate of these
    public PVector2 domeCoordToScreen(LedPixel c, int width, int height) {
	return LayoutUtil.xyToScreen(getLocation(c),
				     width, height, 2 * getRadius(), true);
    }

    public PVector2[] getViewport() {
	return getViewport(0.);
    }
    
    // Returns the bounding rectangular viewport for the dome pixel area (rotated by angle 'rot').
    // 1st vector in the array is the lower left corner; 2nd vector is the width/height.
    public PVector2[] getViewport(double rot) {
        double xmin = getRadius();
        double xmax = -getRadius();
        double ymin = getRadius();
        double ymax = -getRadius();
        for (LedPixel c : coords) {
	    PVector2 p = LayoutUtil.Vrot(getLocation(c), rot);
            xmin = Math.min(xmin, p.x);
            xmax = Math.max(xmax, p.x);
            ymin = Math.min(ymin, p.y);
            ymax = Math.max(ymax, p.y);
        }
        double margin = getPixelBufferRadius();
        xmin -= margin;
        xmax += margin;
        ymin -= margin;
        ymax += margin;

	PVector2 p0 = LayoutUtil.V(xmin, ymin);
        PVector2 pdiag = LayoutUtil.Vsub(LayoutUtil.V(xmax, ymax), p0);
	return new PVector2[] {p0, pdiag};
    }
    
    public abstract double getPixelBufferRadius();
    public abstract double getRadius();

    private void initOpcBuffers() {
	int[] pixelCounts = new int[opcs.size()];
	for (LedPixel c : coords) {
	    pixelCounts[c.getOpcChannel()] += 1;
	}
	opcBuffers = new int[opcs.size()][];
	for (int i = 0; i < opcs.size(); i++) {
	    opcBuffers[i] = new int[pixelCounts[i]];
	}
    }

    public void dispatch() {
	int[] pixelCounts = new int[opcs.size()];
	for (LedPixel c : coords) {
	    int channel = c.getOpcChannel();
	    int i = pixelCounts[channel];
            opcBuffers[channel][i] = getColor(c);
	    pixelCounts[channel] += 1;
	}
	for (int i = 0; i < opcs.size(); i++) {
	    opcs.get(i).dispatch(opcBuffers[i]);
	}
    }
    
    public String getOpcHosts(){
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < opcs.size(); i++) {
	    sb.append(opcs.get(i).getHost());
	    if (i < opcs.size() - 1) {
		sb.append(",");
	    }
	}
	return sb.toString();
    }

}
