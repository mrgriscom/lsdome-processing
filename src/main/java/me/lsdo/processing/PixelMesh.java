
package me.lsdo.processing;

import java.util.*;
//import processing.core.PVector;

public abstract class PixelMesh<T extends LedPixel> {

    List<OPC> opcs;
    private int[][] opcBuffers;

    // Positions of all the pixels in triangular grid coordinates (and in the order seen by
    // the fadecandy).
    public ArrayList<T> coords;
    public PixelTransform transform;

    // Color
    protected HashMap<LedPixel, Integer> colors;

    public PixelMesh() {
	opcs = new ArrayList<OPC>();
	coords = new ArrayList<T>();
	colors = new HashMap<LedPixel, Integer>();
    }

    public void init() {
        for (LedPixel c : coords) {
            setColor(c, 0);
	}	
	initOpcBuffers();
    }
    
    public Integer getColor(LedPixel dCoord){
	return dCoord.spacerPixel ? 0 : colors.get(dCoord);
    }

    public void setColor(LedPixel dCoord, Integer color){
      colors.put(dCoord, color);
    }

    public int getNumPoints(){
        return coords.size();
    }

    public PVector2 domeCoordToScreen(LedPixel c, int width, int height) {
	return LayoutUtil.xyToScreen(transform.transform(c), width, height);
    }

    // Returns the bounding rectangular viewport for the dome pixel area
    // 1st vector in the array is the lower left corner; 2nd vector is the width/height.
    public PVector2[] getViewport() {
        double xmin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
        for (LedPixel c : coords) {
	    PVector2 p = transform.transform(c);
            xmin = Math.min(xmin, p.x);
            xmax = Math.max(xmax, p.x);
            ymin = Math.min(ymin, p.y);
            ymax = Math.max(ymax, p.y);
        }
	PVector2 margin = LayoutUtil.Vmult(transform.getMargins(coords.get(0)), getPixelBufferRadius());
        xmin -= margin.x;
        xmax += margin.x;
        ymin -= margin.y;
        ymax += margin.y;

	PVector2 p0 = LayoutUtil.V(xmin, ymin);
        PVector2 pdiag = LayoutUtil.Vsub(LayoutUtil.V(xmax, ymax), p0);
	return new PVector2[] {p0, pdiag};
    }

    public PixelTransform stretchToViewport(int width, int height) {
	return stretchToViewport(width, height, 1., 1.);
    }
    
    public PixelTransform stretchToViewport(final int width, final int height, final double xscale, final double yscale) {
	PVector2 viewport[] = getViewport();
	final PVector2 p0 = viewport[0];
	final PVector2 pDim = viewport[1];
	return transform.compoundTransform(new LayoutUtil.Transform() {
		double reproject(double p, double p0, double dim, double extent, double scale) {
		    double val = (p - p0) / dim; // normalize [0,1]
		    val = -extent * (1 - val) + extent * val; // normalize [-extent,extent]
		    return val / scale;
		}
		    
		public PVector2 transform(PVector2 p) {
		    return LayoutUtil.V(reproject(p.x, p0.x, pDim.x, (double)width/height, xscale),
					reproject(p.y, p0.y, pDim.y, 1., yscale));
		}
	    });
    }
    
    // Return which of N OPC servers manages this pixel
    public abstract int getOpcChannel(T pixel);
    // This should be roughly one-half of the average spacing between pixels
    public abstract double getPixelBufferRadius();

    private void initOpcBuffers() {
	int[] pixelCounts = new int[opcs.size()];
	for (T c : coords) {
	    pixelCounts[getOpcChannel(c)] += 1;
	}
	opcBuffers = new int[opcs.size()][];
	for (int i = 0; i < opcs.size(); i++) {
	    opcBuffers[i] = new int[pixelCounts[i]];
	}
    }

    public void dispatch() {
	int[] pixelCounts = new int[opcs.size()];
	for (T c : coords) {
	    int channel = getOpcChannel(c);
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
