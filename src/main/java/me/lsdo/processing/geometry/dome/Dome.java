package me.lsdo.processing.geometry.dome;

import java.util.*;
import me.lsdo.processing.PixelMesh;
import me.lsdo.processing.OPC;
import me.lsdo.processing.PixelTransform;
import me.lsdo.processing.util.*;

// Dome geometry, for a few different supported #s of panels

public class Dome extends PixelMesh<DomePixel> {
    
    // Size of single panel's pixel grid.
    public static final int PANEL_SIZE = 15;

    DomeLayoutUtil.PanelConfig config;

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
	config = DomeLayoutUtil.getPanelConfig(layout);
	init();
    }

    protected List<DomePixel> getCoords() {
	return config.fill(PANEL_SIZE);
    }

    protected PixelTransform getDefaultTransform() {
	return config.getDefaultTransform();
    }
    
    public int getOpcChannel(DomePixel pixel) {
	return 0;
    }
    
    public double getPixelBufferRadius() {
	return .5*DomeLayoutUtil.pixelSpacing(PANEL_SIZE);
    }

}
