package me.lsdo.processing;

/*
 * Simple Open Pixel Control client for Processing,
 * designed to sample each LED's color from some point on the canvas.
 *
 * Micah Elizabeth Scott, 2013
 * This file is released into the public domain.
 */

import java.io.*;
import java.net.*;
import me.lsdo.processing.util.*;

public class OPC implements Runnable {
    private Thread thread;
    private Socket socket;
    private OutputStream output, pending;
    private String host;
    private int port;

    private byte[] packetData;
    private byte firmwareConfig;
    private String colorCorrection;

    boolean failedAlready = false;
    
    public OPC()
    {
	this(Config.getConfig().OpcHostname.get(0),
	     Config.getConfig().OpcPort);
    }

    public OPC(String host, int port) {
        this.host = host;
        this.port = port;
	System.out.println("OPC endpoint " + getServer() + "; connecting...");
        thread = new Thread(this);
        thread.start();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getServer() {
	return host + ":" + port;
    }
    
    // Enable or disable dithering. Dithering avoids the "stair-stepping" artifact and increases color
    // resolution by quickly jittering between adjacent 8-bit brightness levels about 400 times a second.
    // Dithering is on by default.
    public void setDithering(boolean enabled) {
        if (enabled)
            firmwareConfig &= ~0x01;
        else
            firmwareConfig |= 0x01;
        sendFirmwareConfigPacket();
    }

    // Enable or disable frame interpolation. Interpolation automatically blends between consecutive frames
    // in hardware, and it does so with 16-bit per channel resolution. Combined with dithering, this helps make
    // fades very smooth. Interpolation is on by default.
    public void setInterpolation(boolean enabled) {
        if (enabled)
            firmwareConfig &= ~0x02;
        else
            firmwareConfig |= 0x02;
        sendFirmwareConfigPacket();
    }

    // Put the Fadecandy onboard LED under automatic control. It blinks any time the firmware processes a packet.
    // This is the default configuration for the LED.
    public void statusLedAuto() {
        firmwareConfig &= 0x0C;
        sendFirmwareConfigPacket();
    }

    // Manually turn the Fadecandy onboard LED on or off. This disables automatic LED control.
    public void setStatusLed(boolean on) {
        firmwareConfig |= 0x04;   // Manual LED control
        if (on)
            firmwareConfig |= 0x08;
        else
            firmwareConfig &= ~0x08;
        sendFirmwareConfigPacket();
    }

    // Set the color correction parameters
    public void setColorCorrection(float gamma, float red, float green, float blue) {
        colorCorrection = "{ \"gamma\": " + gamma + ", \"whitepoint\": [" + red + "," + green + "," + blue + "]}";
        sendColorCorrectionPacket();
    }

    // Set custom color correction parameters from a string
    public void setColorCorrection(String s) {
        colorCorrection = s;
        sendColorCorrectionPacket();
    }

    // Send a packet with the current firmware configuration settings
    private void sendFirmwareConfigPacket() {
        if (pending == null) {
            // We'll do this when we reconnect
            return;
        }

        byte[] packet = new byte[9];
        packet[0] = 0;          // Channel (reserved)
        packet[1] = (byte) 0xFF; // Command (System Exclusive)
        packet[2] = 0;          // Length high byte
        packet[3] = 5;          // Length low byte
        packet[4] = 0x00;       // System ID high byte
        packet[5] = 0x01;       // System ID low byte
        packet[6] = 0x00;       // Command ID high byte
        packet[7] = 0x02;       // Command ID low byte
        packet[8] = firmwareConfig;

        try {
            pending.write(packet);
        } catch (Exception e) {
            dispose(e);
        }
    }

    // Send a packet with the current color correction settings
    private void sendColorCorrectionPacket() {
        if (colorCorrection == null) {
            // No color correction defined
            return;
        }
        if (pending == null) {
            // We'll do this when we reconnect
            return;
        }

        byte[] content = colorCorrection.getBytes();
        int packetLen = content.length + 4;
        byte[] header = new byte[8];
        header[0] = 0;          // Channel (reserved)
        header[1] = (byte) 0xFF; // Command (System Exclusive)
        header[2] = (byte) (packetLen >> 8);
        header[3] = (byte) (packetLen & 0xFF);
        header[4] = 0x00;       // System ID high byte
        header[5] = 0x01;       // System ID low byte
        header[6] = 0x00;       // Command ID high byte
        header[7] = 0x01;       // Command ID low byte

        try {
            pending.write(header);
            pending.write(content);
        } catch (Exception e) {
            dispose(e);
        }
    }

    // Automatically called at the end of each draw().
    // This handles the automatic Pixel to LED mapping.
    // If you aren't using that mapping, this function has no effect.
    // In that case, you can call setPixelCount(), setPixel(), and writePixels()
    // separately.
    public void dispatch(int[] buffer) {
        int numPixels = buffer.length;
	if (packetData == null) {
	    initPacketData(numPixels);
	}
	
        int offset = 4;
        for (int i = 0; i < numPixels; i++) {
            int pixel = buffer[i];
            packetData[offset] = (byte) (pixel >> 16);
            packetData[offset + 1] = (byte) (pixel >> 8);
            packetData[offset + 2] = (byte) pixel;
            offset += 3;
        }

        writePixels();
    }

    void initPacketData(int numPixels) {
	int ledBytes = 3 * numPixels;
	packetData = new byte[4 + ledBytes];
	packetData[0] = 0;  // Channel
	packetData[1] = 0;  // Command (Set pixel colors)
	packetData[2] = (byte) (ledBytes >> 8);
	packetData[3] = (byte) (ledBytes & 0xFF);
    }
    
    // Transmit our current buffer of pixel values to the OPC server. This is handled
    // automatically in draw() if any pixels are mapped to the screen, but if you haven't
    // mapped any pixels to the screen you'll want to call this directly.
    void writePixels() {
        if (packetData == null || packetData.length == 0) {
            // No pixel buffer
            return;
        }
        if (output == null) {
            return;
        }

        try {
            output.write(packetData);
        } catch (Exception e) {
            dispose(e);
        }
    }

    void dispose(Exception e) {
	if (e != null) {
	    System.out.println(getServer() + " connection error: " + e);
	    if (!failedAlready) {
		e.printStackTrace();
	    }
	}
	
        // Destroy the socket. Called internally when we've disconnected.
        // (Thread continues to run)
        if (output != null) {
            System.out.println("Disconnected from OPC " + getServer());
        }
        socket = null;
        output = pending = null;
	failedAlready = true;
    }

    public void run() {
        // Thread tests server connection periodically, attempts reconnection.
        // Important for OPC arrays; faster startup, client continues
        // to run smoothly when mobile servers go in and out of range.
        for (; ; ) {

            if (output == null) { // No OPC connection?
                try {              // Make one!
                    socket = new Socket(host, port);
                    socket.setTcpNoDelay(true);
                    pending = socket.getOutputStream(); // Avoid race condition...
                    System.out.println("OPC " + getServer() + " connected");
		    failedAlready = false;
                    sendColorCorrectionPacket();        // These write to 'pending'
                    sendFirmwareConfigPacket();         // rather than 'output' before
                    output = pending;                   // rest of code given access.
                    // pending not set null, more config packets are OK!
                } catch (ConnectException e) {
                    dispose(e);
                } catch (IOException e) {
                    dispose(e);
                }
            }

            // Pause thread to avoid massive CPU load
            try {
                thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
    }
}
