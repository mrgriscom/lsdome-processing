/**
 * The abstract structure of the dome. No aware of any sketches.
 *
 * Config is pulled in from elsewhere, and the layout is mostly done by layout util, so class is a little bit
 * thin right now.
 */

package me.lsdo.processing;

import java.util.*;
//import processing.core.PVector;

public class Dome extends PixelMesh<DomePixel> {

    // Size of a single panel's pixel grid
    private int panel_size;

    // Distance from center to farthest pixel, in panel lengths
    private double radius;

    public Dome(OPC opc) {
        this(Config.getConfig().numPanels, opc);
    }

    public Dome(int numPanels, OPC opc) {
	this(DomeLayoutUtil.getPanelLayoutForNumPanels(numPanels), opc);
	System.out.println(String.format("Using %d-panel layout", numPanels));
    }

    protected Dome(PanelLayout layout, OPC opc) {
	super();

	opcs.add(opc);
	
        // e.g. 15
        panel_size = Config.PANEL_SIZE;

	DomeLayoutUtil.PanelConfig config = DomeLayoutUtil.getPanelConfig(layout);
	
        coords.addAll(config.fill(panel_size));
	transform = config.getDefaultTransform();
	init();

	radius = config.radius;
    }

    public int getOpcChannel(DomePixel pixel) {
	return 0;
    }
    
    public double getPixelBufferRadius() {
	return .5*DomeLayoutUtil.pixelSpacing(panel_size);
    }
    
    public double getRadius(){
        return radius;
    }

}
