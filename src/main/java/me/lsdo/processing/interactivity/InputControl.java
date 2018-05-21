package me.lsdo.processing.interactivity;

import java.io.*;
import java.util.*;

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

    public static final int PORT = 5556;
    
    Map<String, InputHandler> handlers;
    Socket subscriber;

    public static class InputHandler {
        public void button(boolean pressed) {
            throw new RuntimeException("handler did not override!");
        }

        public void slider(double val) {
            throw new RuntimeException("handler did not override!");
        }

        public void jog(boolean inc) {
            throw new RuntimeException("handler did not override!");
        }

	public void setRaw(String s) {
	    if (s.equals("true")) {
		set(true);
	    } else if (s.equals("false")) {
		set(false);
	    } else {
		try {
		    set(Double.parseDouble(s));
		    return;
		} catch (NumberFormatException nfe) {
		}

		set(s);
	    }
	}

	public void set(String s) {
            throw new RuntimeException("handler did not override!");
	}
	
	public void set(double d) {
            throw new RuntimeException("handler did not override!");
	}

	public void set(boolean b) {
            throw new RuntimeException("handler did not override!");
	}
    }

    public InputControl() {
	handlers = new HashMap<String, InputHandler>();
    }
    
    public void init() {
	Context context = ZMQ.context(1);
        subscriber = context.socket(ZMQ.SUB);
        subscriber.connect("tcp://localhost:" + PORT);
        subscriber.subscribe(new byte[0]);
    }

    public void registerHandler(String controlName, InputHandler handler) {
        handlers.put(controlName, handler);
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

    void processInputEvent(String event) {
        String[] parts = event.split(":");
        if (parts.length != 4) {
            System.err.println("can't understand " + event);
            return;
        }
	String uuid = parts[0];
	String device = parts[1];
        String name = parts[2];
        String evt = parts[3];
        InputHandler handler = handlers.get(name);
        if (handler == null) {
            return;
        }

        ControlType type;
        boolean boolVal = false;
        double realVal = -1;
	String strVal = null;

	if (evt.startsWith("~")) {
	    type = ControlType.RAW;
	    strVal = evt.substring(1);	
        } else if (evt.equals("press")) {
            type = ControlType.BUTTON;
            boolVal = true;
        } else if (evt.equals("release")) {
            type = ControlType.BUTTON;
            boolVal = false;
        } else if (evt.equals("inc")) {
            type = ControlType.JOG;
            boolVal = true;
        } else if (evt.equals("dec")) {
            type = ControlType.JOG;
            boolVal = false;
        } else {
            type = ControlType.SLIDER;
            realVal = Double.parseDouble(evt);
            if (realVal < 0. || realVal > 1.) {
                System.err.println("slider out of range " + realVal);
            }
        }

        switch (type) {
	case RAW:
	    handler.setRaw(strVal);
	    break;
        case BUTTON:
            handler.button(boolVal);
            break;
        case SLIDER:
            handler.slider(realVal);
            break;
        case JOG:
            handler.jog(boolVal);
            break;
        }
    }
}