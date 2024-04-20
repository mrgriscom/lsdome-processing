// Global configuration variables.
package me.lsdo.processing.util;

import java.util.*;
import java.io.*;
import me.lsdo.processing.OPC;

public class Config {

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 7890;
    public static final int DEFAULT_PANELS = 24;
    public static final String[] knownGeometries = {"lsdome", "prometheus"};
    public static final int[] DEFAULT_ZMQ_IN_OUT = {5556, 5557};

    private Properties domeProps = new Properties();
    private Properties sketchProps = new Properties();

    static long startMillis = -1;

    public static double clock() {
	long now = System.currentTimeMillis();
	if (startMillis == -1) {
	    startMillis = now;
	}
	return (now - startMillis) / 1000.;
    }

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

    private List<String> getMultiProperty(Properties props, String propName, String defaultValue) {
        List<String> propVals = new ArrayList<String>();
	int i = 1;
	while (true) {
	    String prop = (i == 1 ?
                           props.getProperty(propName, defaultValue) :
                           props.getProperty(propName + i, ""));
	    if (prop.isEmpty()) {
		break;
	    }
            propVals.add(prop);
	    i += 1;
	}
        return propVals;
    }

    private Config()
    {
	loadProperties(domeProps, "config.properties");
	loadProperties(sketchProps, "sketch.properties");

	OpcHostname = getMultiProperty(domeProps, "opchostname", DEFAULT_HOST);
        layoutPaths = getMultiProperty(domeProps, "layout", "");
	OpcPort = getProperty(domeProps, "opcport", DEFAULT_PORT);
	numPanels = getProperty(domeProps, "num_panels", DEFAULT_PANELS);
	geomType = domeProps.getProperty("geometry", "");
	zmqPortIn = getProperty(domeProps, "zmq_port_inbound", DEFAULT_ZMQ_IN_OUT[0]);
	zmqPortOut = getProperty(domeProps, "zmq_port_outbound", DEFAULT_ZMQ_IN_OUT[1]);

	if (geomType.isEmpty()) {
	    geomType = null;
	} else {
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

    public List<String> OpcHostname;
    public int OpcPort;
    public String geomType;
    public int numPanels;
    public List<String> layoutPaths;
    public int zmqPortIn;
    public int zmqPortOut;

    public OPC[] makeOPCs(int n) {
	OPC[] opcs = new OPC[n];
	for (int i = 0; i < n; i++) {
	    OPC opc = (i < OpcHostname.size() ?
		       new OPC(OpcHostname.get(i), OpcPort) :
		       new OPC(OpcHostname.get(0), OpcPort + i - OpcHostname.size() + 1));
	    opcs[i] = opc;
	}
	return opcs;
    }

}
