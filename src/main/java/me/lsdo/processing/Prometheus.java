/**
 * The abstract structure of the dome. No aware of any sketches.
 *
 * Config is pulled in from elsewhere, and the layout is mostly done by layout util, so class is a little bit
 * thin right now.
 */

package me.lsdo.processing;

import java.util.*;
import java.io.*;
import com.google.gson.*;
import com.google.gson.stream.*;

public class Prometheus extends PixelMesh<WingPixel> {

    static final String LAYOUT_PATH = "/home/drew/dev/lsdome/lsdome/src/config/simulator_layouts/prometheus_wing.json";
    static final double PLATFORM_WIDTH = 1.; // m
    static final double WINGSPAN = 10.; // m
    
    static class LayoutPoint {
	double[] point;
    }
    
    public Prometheus(OPC opcLeft, OPC opcRight) {
	super();

	opcs.add(opcLeft);
	opcs.add(opcRight);

	List<PVector2> pixels;
	try {
	    pixels = loadPixels(LAYOUT_PATH);
	} catch (IOException e) {
	    throw new RuntimeException("can't load wing pixel layout json");
	}
	for (int i = 0; i < pixels.size(); i++) {
	    for (int wing = 0; wing < 2; wing++) {
		coords.add(new WingPixel(wing, i, pixels.get(i)));
	    }
	}

	transform = PixelTransform.simpleTransform(new LayoutUtil.Transform(){
		public PVector2 transform(PVector2 p) { return LayoutUtil.Vmult(p, 1./WINGSPAN); }
	    });
	
	init();
    }

    private List<PVector2> loadPixels(String path) throws IOException {
	Gson gson = new Gson();
	InputStream is = new BufferedInputStream(new FileInputStream(new File(path)));
	JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));

        List<PVector2> points = new ArrayList<PVector2>();
        reader.beginArray();
        while (reader.hasNext()) {
	    LayoutPoint lp = gson.fromJson(reader, LayoutPoint.class);
	    double x = lp.point[0];
	    double y = lp.point[1];
	    double z = lp.point[2];
	    if (z != 0 || (x == -5 && y == -5) /* old method*/) {
		// spacer pixel
		points.add(null);
	    } else {
		points.add(LayoutUtil.V(x, y));
	    }
        }
        reader.endArray();
        reader.close();

	double minX = Double.POSITIVE_INFINITY;
	for (PVector2 p : points) {
	    if (p != null) {
		minX = Math.min(minX, p.x);
	    }
	}
        List<PVector2> realignedPoints = new ArrayList<PVector2>();
	for (PVector2 p : points) {
	    if (p == null) {
		// spacer pixel
		realignedPoints.add(null);
	    } else {
		// Note: assuming y-axis is centered properly
		realignedPoints.add(LayoutUtil.V(p.x - minX + .5*PLATFORM_WIDTH, p.y));
	    }
	}
        return realignedPoints;
    }
    
    public int getOpcChannel(WingPixel pixel) {
	return pixel.wing;
    }
    
    public double getPixelBufferRadius() {
	double spacing = .15; // m
	return .5 * spacing * .7; // reduce to 70% to account for denser areas of wing
    }

}
