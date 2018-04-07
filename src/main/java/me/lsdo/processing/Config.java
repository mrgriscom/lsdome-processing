// Global configuration variables.
package me.lsdo.processing;

import java.util.*;
import java.io.*;



public class Config {

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 7890;
    public static final int DEFAULT_PANELS = 24;
    public static final String[] knownGeometries = {"lsdome", "prometheus"};

    private Properties domeProps = new Properties();
    private Properties sketchProps = new Properties();
    
    private static class ConfigInstance {
        public static Config config = new Config();
    }

    public static Config getConfig() {
        return ConfigInstance.config;
    }

    private void loadProperties(Properties props, String filename) {
        try {        
            String workingDir = System.getProperty("user.dir");
            System.out.println(String.format("Looking for %s in %s", filename, workingDir));
	    props.load(new FileInputStream(filename));
        } catch (IOException e) {
            System.err.println("could not load " + filename);
        }
    }
    
    private Config()
    {
	loadProperties(domeProps, "config.properties");
	loadProperties(sketchProps, "sketch.properties");

	OpcHostname = domeProps.getProperty("opchostname", DEFAULT_HOST);
	OpcPort = getProperty(domeProps, "opcport", DEFAULT_PORT);
	numPanels = getProperty(domeProps, "num_panels", DEFAULT_PANELS);
	geomType = domeProps.getProperty("geometry", "");

	boolean validGeomType = false;
	for (String s : knownGeometries) {
	    if (s.equals(geomType)) {
		validGeomType = true;
		break;
	    }
	}
	if (!validGeomType) {
	    System.out.println("valid 'geometry' property required; one of:");
	    for (String s : knownGeometries) {
		System.out.println(s);
	    }
	    throw new RuntimeException("invalid geometry type '" + geomType + "'");
	}
    }

    private static int getProperty(Properties props, String key, int defaultValue) {
	return Integer.parseInt(props.getProperty(key, "" + defaultValue));
    }
    
    private static double getProperty(Properties props, String key, double defaultValue) {
	return Double.parseDouble(props.getProperty(key, "" + defaultValue));
    }
    
    private static boolean getProperty(Properties props, String key, boolean defaultValue) {
	String val = props.getProperty(key, defaultValue ? "true" : "false");
	if (val.equals("true")) {
	    return true;
	} else if (val.equals("false")) {
	    return false;
	} else {
	    throw new RuntimeException(String.format("property value for %s must be 'true' or 'false'; is: %s", key, val));
	}
    }
    
    public static String getSketchProperty(String key, String defaultValue) {
	return getConfig().sketchProps.getProperty(key, defaultValue);
    }
    
    public static int getSketchProperty(String key, int defaultValue) {
	return getProperty(getConfig().sketchProps, key, defaultValue);
    }
    
    public static double getSketchProperty(String key, double defaultValue) {
	return getProperty(getConfig().sketchProps, key, defaultValue);
    }
    
    public static boolean getSketchProperty(String key, boolean defaultValue) {
	return getProperty(getConfig().sketchProps, key, defaultValue);
    }
    
    // Debug mode.
    static final boolean DEBUG = false;

    public String OpcHostname;
    public int OpcPort;
    public String geomType;
    public int numPanels;
    
    // Size of single panel's pixel grid.
    static final int PANEL_SIZE = 15;

}
