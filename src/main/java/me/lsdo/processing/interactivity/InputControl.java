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
    }
    
    public void init() {
	Context context = ZMQ.context(1);
        subscriber = context.socket(ZMQ.SUB);
        subscriber.connect("tcp://localhost:" + Config.getConfig().zmqPortIn);
        subscriber.subscribe(new byte[0]);

	publisher = context.socket(ZMQ.PUB);
        publisher.bind("tcp://*:" + Config.getConfig().zmqPortOut);
    }

    public void broadcast(String msg) {
	publisher.send(msg);
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
	ParameterJson params[];
    }
    
    static class ParameterJson {
	String name;
	String category;

	boolean isAction;
	boolean isEnum;
	boolean isNumeric;

	String[] values;
	String[] captions;

	boolean isBounded;
	boolean isInt;
    }
    
    public void finalizeParams() {
	for (Parameter p : Parameter.parameters) {
	    p.bind(this);
	    System.out.println(p.name);
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
	} else if (evt.eventType.equals("button")) {
	    boolean pressed;
	    if (evt.value.equals("true")) {
		pressed = true;
	    } else if (evt.value.equals("false")) {
		pressed = false;
	    } else {
		return;
	    }
	    handler.button(pressed);
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
