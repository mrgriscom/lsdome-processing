import java.util.*;
import processing.core.*;
import me.lsdo.processing.*;

public class TwinkleSketch extends PixelGridAnimation{

    // Skew of initial brightness of stars. Higher means fewer bright stars.
    // >= 1.
    final double POWER_LAW = 15;

    // Aggressiveness of twinkling. Higher means greater range and more apparent
    // variation.
    // >= 0.
    final double TWINKLE_FACTOR = .2;

    // Maximum allowed saturation for stars at max brightness.
    // [0, 1]
    final double MAX_SAT_FULL_BRIGHTNESS = .2;

    // Dampening factor for saturation with increasing brightness. Higher means
    // saturated stars visible only with lower brightness.
    // >= 0.
    final double SAT_V_BRIGHTNESS_POWER_LAW = 2;

    final double HUE = 0.6666;  // deep blue

    // Perform this many twinkling iterations per frame.
    final int SIMULATED_SPEEDUP = 1;

    private HashMap<DomeCoord, Double> brightness;
    private HashMap<DomeCoord, Double> saturation;

    public TwinkleSketch(Dome dome, OPC opc) {
        super(dome, opc);
        graphics.colorMode(PApplet.HSB,256);
        brightness = new HashMap<DomeCoord, Double>();
        saturation = new HashMap<DomeCoord, Double>();
        for (DomeCoord c : dome.coords) {
            brightness.put(c, Math.pow(Math.random(), POWER_LAW));
            saturation.put(c, Math.random());
        }
    }

    public int drawPixel(DomeCoord c, double t) {
        double b = brightness.get(c);
        for (int i = 0; i < SIMULATED_SPEEDUP; i++) {
            double rand = 2*(Math.random() - .5);
            b *= Math.exp(TWINKLE_FACTOR * rand);

            // Capping only at one end violates the 'regress to mean' property of a random walk.
            // I didn't like the look from capping on the low end too, though. This means everything
            // will gradually get dimmer. This is a good analogue for the heat death of the universe.
            b = Math.min(b, 1.);
        }
        brightness.put(c, b);

        double sat = saturation.get(c);
        double maxsat = MAX_SAT_FULL_BRIGHTNESS + (1 - MAX_SAT_FULL_BRIGHTNESS) * Math.pow(1 - b, SAT_V_BRIGHTNESS_POWER_LAW);
        sat *= maxsat;

        return graphics.color((int)(HUE * 255), (int)(sat * 255), (int)(b * 255));
    }

}
