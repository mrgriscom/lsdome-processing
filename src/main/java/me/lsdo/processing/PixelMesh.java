
package me.lsdo.processing;

import java.util.*;
import me.lsdo.processing.util.*;
import me.lsdo.processing.interactivity.*;

// Abstract base class of representation of mesh geometry
// Maintains the xy positions of all pixels
// Handles pushing pixel data to OPC
// Manages the top-level transform of real-world pixel xy coordinates (in meters) to normalized virtual canvas-space coordinates ([-1,1])

public abstract class PixelMesh<T extends LedPixel> {

    public List<OPC> opcs;
    private int[][] opcBuffers;

    // All pixels, in the order seen by the fadecandies (including spacer pixels).
    private ArrayList<T> _coords;
    // Just visible pixels (spacer pixels filtered out)
    private ArrayList<T> visibleCoords;
    // Mapping of pixels to current color (just visible pixels)
    private HashMap<LedPixel, Integer> colors;
    // For consumers to iterate over just real pixels (not spacer pixels)
    public List<T> coords() {
	return visibleCoords;
    }
    // Return all pixels (including spacer pixels) for diagnostic sketches (DON'T NORMALLY USE THIS)
    public List<T> _allPixels() {
	return _coords;
    }

    public PixelTransform _transform;
    public boolean txChanged = false;

    
    public class PlacementParameter extends NumericParameter {
	public PlacementParameter(String name) {
	    super(name, "placement");
	}
	    
	@Override
	public void onChange(Double prev) {
	    txChanged = true;
	}
    }
    
    public class AnglePlacementParameter extends NumericParameter.Angle {
	public AnglePlacementParameter(String name) {
	    super(name, "placement");
	}
	    
	@Override
	public void onChange(Double prev) {
	    txChanged = true;
	}
    }
    
    public class BoolPlacementParameter extends BooleanParameter {
	public BoolPlacementParameter(String name) {
	    super(name, "placement");
	}
	    
	@Override
	public void onChange(Boolean prev) {
	    txChanged = true;
	}
    }
    
    public class EnumPlacementParameter<T extends Enum<T>> extends EnumParameter<T> {
	public EnumPlacementParameter(String name, Class<T> enumClass) {
	    super(name, "placement", enumClass);
	}
	    
	@Override
	public void onChange(T prev) {
	    txChanged = true;
	}
    }
    
    public class PlacementTransform extends PixelTransform {
	PlacementParameter xo;
	PlacementParameter yo;
	AnglePlacementParameter rot;
	PlacementParameter scale;
	
	public PlacementTransform() {
	    xo = new PlacementParameter("x-offset");
	    xo.setSensitivity(.01);
	    xo.init(Config.getSketchProperty("placement_xo", 0.));
	    
	    yo = new PlacementParameter("y-offset");
	    yo.setSensitivity(.01);
	    yo.init(Config.getSketchProperty("placement_yo", 0.));

	    rot = new AnglePlacementParameter("rotation");
	    rot.setSensitivity(.01 * 180);
	    rot.init(Config.getSketchProperty("placement_rot", 0.));

	    scale = new PlacementParameter("scale");
	    scale.scale = NumericParameter.Scale.LOG;
	    scale.setSensitivity(.01);
	    scale.max = 3.;
	    scale.min = 1./scale.max;
	    scale.softLimits = true;
	    scale.init(Config.getSketchProperty("placement_scale", 1.));
	}
	
	public PVector2 transform(PVector2 p) {
	    return LayoutUtil.Vadd(LayoutUtil.Vmult(LayoutUtil.Vrot(p, rot.getInternal()), scale.get()), LayoutUtil.V(xo.get(), yo.get()));
	}
    }
    public PlacementTransform placement;
    
    public PixelMesh() {
	opcs = new ArrayList<OPC>();
	_coords = new ArrayList<T>();
	visibleCoords = new ArrayList<T>();
	colors = new HashMap<LedPixel, Integer>();
    }

    // child implementations must call this at the end of their constructor
    public void init() {
	_coords.addAll(getCoords());
        for (T c : _coords) {
            setColor(c, 0);
	    if (!c.spacerPixel) {
		visibleCoords.add(c);
	    }
	}	
	initOpcBuffers();
    }

    protected abstract List<T> getCoords();

    public PixelTransform transform() {
	if (_transform == null) {
	    placement = new PlacementTransform();
	    _transform = getDefaultTransform();
	    _transform = _transform.compoundTransform(placement);
	    PixelTransform postPlacementTx = getPostPlacementTransform();
	    if (postPlacementTx != null) {
		_transform = _transform.compoundTransform(postPlacementTx);
	    }
	}
	return _transform;
    }
    
    protected abstract PixelTransform getDefaultTransform();

    protected PixelTransform getPostPlacementTransform() {
	return null;
    }

    public void beforeDraw(PixelMeshAnimation anim) {
	if (txChanged) {
	    if (anim instanceof PixelTransform.TransformListener) {
		((PixelTransform.TransformListener)anim).transformChanged();
	    }
	    txChanged = false;
	}
    }
    
    public Integer getColor(LedPixel dCoord){
	return dCoord.spacerPixel ? 0 : colors.get(dCoord);
    }

    public void setColor(LedPixel dCoord, Integer color){
	if (dCoord.spacerPixel) {
	    return;
	}
	colors.put(dCoord, color);
    }

    public int getNumPoints(){
        return coords().size();
    }

    // Returns the bounding rectangular viewport for the dome pixel area
    // 1st vector in the array is the lower left corner; 2nd vector is the width/height.
    public PVector2[] getViewport() {
        double xmin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
        for (LedPixel c : coords()) {
	    PVector2 p = transform().transform(c);
            xmin = Math.min(xmin, p.x);
            xmax = Math.max(xmax, p.x);
            ymin = Math.min(ymin, p.y);
            ymax = Math.max(ymax, p.y);
        }
	PVector2 margin = LayoutUtil.Vmult(transform().getMargins(coords().get(0)), getPixelBufferRadius());
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
	return stretchToViewport(width, height, xscale, yscale, 0., 0.);
    }
    
    public PixelTransform stretchToViewport(final int width, final int height, final double xscale, final double yscale, final double xo, final double yo) {
	PVector2 viewport[] = getViewport();
	final PVector2 p0 = viewport[0];
	final PVector2 pDim = viewport[1];
	return new PixelTransform() {
	    double reproject(double p, double p0, double dim, double extent, double scale) {
		double val = (p - p0) / dim; // normalize [0,1]
		val = -extent * (1 - val) + extent * val; // normalize [-extent,extent]
		return val / scale;
	    }
		    
	    public PVector2 transform(PVector2 p) {
		return LayoutUtil.Vadd(LayoutUtil.V(reproject(p.x, p0.x, pDim.x, (double)width/height, xscale),
						    reproject(p.y, p0.y, pDim.y, 1., yscale)),
				       LayoutUtil.V(xo, yo));
	    }
	};
    }
    
    // Return which of N OPC servers manages this pixel
    public abstract int getOpcChannel(T pixel);
    // This should be roughly one-half of the average spacing between pixels
    public abstract double getPixelBufferRadius();

    private void initOpcBuffers() {
	int[] pixelCounts = new int[opcs.size()];
	for (T c : _coords) {
	    pixelCounts[getOpcChannel(c)] += 1;
	}
	opcBuffers = new int[opcs.size()][];
	for (int i = 0; i < opcs.size(); i++) {
	    opcBuffers[i] = new int[pixelCounts[i]];
	}
    }

    public void dispatch() {
	int[] pixelCounts = new int[opcs.size()];
	for (T c : _coords) {
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
