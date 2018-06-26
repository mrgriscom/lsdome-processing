// A dome-enabled processing sketch. You should be able to run this in processing
// with the lsdome-processing contributed library, and have the output piped to
// the dome pixels via OPC.

// The necessary additions to dome-enable the sketch are marked with //@@@@

//@@@@@@@@@@@@@@@
import me.lsdo.processing.*;
import me.lsdo.processing.geometry.dome.*;
ProcessingAnimation canvas;
//@@@@@@@@@@@@@@@

int pitch = 50;

void setup()
{
    size(300, 300);
    colorMode(HSB, 256);

    //@@@@@@@@@@@@@@@
    canvas = new ProcessingAnimation(this, new Dome(new OPC()));
    //@@@@@@@@@@@@@@@
}

void draw()
{
    background(0);

    float t = millis();
    float xpos = t/20;

    for (int i = 0; i < 3; i++) {

        fill(i * 80, 192, 255);
        
        float thisPos = (xpos + i * (2 * pitch)) % width;
        rect (thisPos, 0, pitch, height);

        if (thisPos > width - pitch)
            rect (thisPos - width, 0, pitch, height);
    }
    
    //@@@@@@@@@@@@@@@
    canvas.draw();
    //@@@@@@@@@@@@@@@
}

