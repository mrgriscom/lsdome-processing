package me.lsdo.processing;

import java.util.*;

// Dome geometry, for a few different supported #s of panels

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

}
