/*
 * Fractal noise animation. Modified version of Micah Scott's code at 
 * https://github.com/scanlime/fadecandy/tree/master/examples/processing/grid24x8z_clouds
 */
import me.lsdo.processing.*;

PixelGridSketch sketch;

void setup() {
    size(300, 300);
    Dome dome = new Dome();
    OPC opc = new OPC("127.0.0.1", 7890);
    DomeAnimation animation = new CloudsSketch(dome, opc);
    sketch = new PixelGridSketch(this, animation);
}

void draw() {
    sketch.draw();
}
