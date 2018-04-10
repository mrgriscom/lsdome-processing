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
            points.add(LayoutUtil.V(lp.point[0], lp.point[1]));
        }
        reader.endArray();
        reader.close();

	double minX = Double.POSITIVE_INFINITY;
	for (PVector2 p : points) {
	    minX = Math.min(minX, p.x);
	}
        List<PVector2> realignedPoints = new ArrayList<PVector2>();
	for (PVector2 p : points) {
	    realignedPoints.add(LayoutUtil.V(p.x - minX + .5*PLATFORM_WIDTH, p.y));
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
    
    public double getRadius(){
        return 9.; // ?
    }

}
