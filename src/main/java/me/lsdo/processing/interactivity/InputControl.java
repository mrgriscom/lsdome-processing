package me.lsdo.processing.interactivity;

import java.io.*;
import java.util.*;
import com.google.gson.*;
import me.lsdo.processing.util.*;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

enum ControlType {
    RAW,
    BUTTON,
    SLIDER,
    JOG
}

public class InputControl {

    Map<String, InputHandler> handlers;
    Socket subscriber;
    Socket publisher;

    public static class InputHandler {
	public void set(String val) {
            throw new RuntimeException("handler did not override!");
	}

        public void button(boolean pressed) {
            throw new RuntimeException("handler did not override!");
        }

        public void slider(double val) {
            throw new RuntimeException("handler did not override!");
        }

        public void jog(double jump) {
            throw new RuntimeException("handler did not override!");
        }

	public boolean customType(String type, String value) {
	    return false;
	}
    }

    public InputControl() {
	handlers = new HashMap<String, InputHandler>();

	registerHandler("_paraminfo", new InputHandler() {
		public void set(String val) {
		    broadcastParams();
		}
	    });
	registerHandler("_placement", new InputHandler() {
		public void set(String val) {
		    broadcastPlacement();
		}
	    });
	registerHandler("_txinfo", new InputHandler() {
		public void set(String val) {
		    broadcastTransform(null);
		}
	    });
    }

    public void init() {
	Context context = ZMQ.context(1);
        subscriber = context.socket(ZMQ.SUB);
        subscriber.connect("tcp://localhost:" + Config.getConfig().zmqPortIn);
        subscriber.subscribe(new byte[0]);

	publisher = context.socket(ZMQ.PUSH);
        publisher.connect("tcp://localhost:" + Config.getConfig().zmqPortOut);
    }

    public void broadcast(Object o) {
	Gson gson = new Gson();
	broadcast(gson.toJson(o));
    }

    public void broadcast(String msg) {
	// push sockets block if the queue is full; which will happen
	// if no one is listening on the other end; set dontwait to avoid
	publisher.send(msg, ZMQ.DONTWAIT);
    }

    public void registerHandler(String controlName, InputHandler handler) {
        handlers.put(controlName, handler);
    }

    static class InputEvent {
	String name;
	String eventType;
	String value;
    }

    static class ParametersJson {
	String type = "params";
	List<ParameterJson> params = new ArrayList<ParameterJson>();
    }

    static class ParameterJson {
	String name;
	String category;
	String description;

	boolean isAction;
        boolean isMomentary;
	boolean isEnum;
	boolean isNumeric;

	String[] values;
	String[] captions;

	boolean isBounded;
	boolean isInt;
    }

    static class ParameterValueJson {
	String type = "param_value";
	String name;
	String value;
	double sliderPos;
    }

    static class PlacementJson {
	String type = "placement";
	List<ParameterValueJson> params = new ArrayList<ParameterValueJson>();
    }

    public static class TransformJson {
        String type = "transform";
        public List<LinearTransformJson> txs = new ArrayList<LinearTransformJson>();
    }
    public static class LinearTransformJson {
        public LinearTransformJson(PVector2 origin, PVector2 U, PVector2 V) {
            this.origin = new Vector2Json(origin);
            this.U = new Vector2Json(U);
            this.V = new Vector2Json(V);
        }

        public Vector2Json origin;
        public Vector2Json U;
        public Vector2Json V;
    }
    public static class Vector2Json {
        public Vector2Json(PVector2 xy) {
            this.x = xy.x;
            this.y = xy.y;
        }

        public double x;
        public double y;
    }


    public static class DurationControlJson {
	String type = "duration";
	public double duration;
    }

    public static class AspectRatioJson {
	String type = "aspect";
	public double aspect;
    }

    public void finalizeParams() {
	for (Parameter p : Parameter.parameters) {
	    p.bind(this);
	}
	broadcastParams();
    }

    public void broadcastParams() {
	ParametersJson allParams = new ParametersJson();
	for (Parameter p : Parameter.parameters) {
	    allParams.params.add(p.toJson());
	}
	broadcast(allParams);
	for (Parameter p : Parameter.parameters) {
	    p.broadcastValue();
	}
    }

    public void broadcastPlacement() {
	PlacementJson pl = new PlacementJson();
	for (Parameter p : Parameter.parameters) {
	    if (p.category.equals("placement")) {
		pl.params.add(p.toValueJson());
	    }
	}
	broadcast(pl);
    }

    // cache this so we can re-broadcast as needed
    TransformJson lastTx = null;
    public void broadcastTransform(TransformJson tx) {
        if (tx != null) {
            this.lastTx = tx;
        }
        if (this.lastTx != null) {
            this.broadcast(this.lastTx);
        }
    }

    public void processInput() {
	while (true) {
	    String msg = subscriber.recvStr(ZMQ.NOBLOCK);
	    if (msg == null) {
		break;
	    }
	    processInputEvent(msg);
        }
    }

    void processInputEvent(String msg) {
	Gson gson = new Gson();
	InputEvent evt;
	try {
	    evt = gson.fromJson(msg, InputEvent.class);
	} catch (JsonParseException e) {
            System.err.println("can't understand " + msg);
            return;
	}

        InputHandler handler = handlers.get(evt.name);
        if (handler == null) {
            return;
        }

	if (evt.eventType.equals("set")) {
	    handler.set(evt.value);
	} else if (evt.eventType.equals("press")) {
	    handler.button(true);
	} else if (evt.eventType.equals("release")) {
	    handler.button(false);
	} else if (evt.eventType.equals("slider")) {
	    try {
		handler.slider(Double.parseDouble(evt.value));
	    } catch (NumberFormatException nfe) {
	    }
	} else if (evt.eventType.equals("jog")) {
	    try {
		handler.jog(Double.parseDouble(evt.value));
	    } catch (NumberFormatException nfe) {
	    }
	} else {
	    boolean handled = handler.customType(evt.eventType, evt.value);
	    if (!handled) {
		throw new RuntimeException("handler did not handle!");
	    }
	}
    }
}
