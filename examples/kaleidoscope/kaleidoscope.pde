import me.lsdo.processing.*;

KaleidoscopeSketch kaleido;

Dome dome;
OPC opc;

void setup() {
    size(300, 300);
    dome = new Dome(width);
    dome.init();
    opc = new OPC(this, "127.0.0.1", 7890);
    opc.setDome(dome);
    kaleido = new KaleidoscopeSketch(this ,width, dome);
    //kaleido.init();
    colorMode(HSB,255);
}

void draw() {
    
    kaleido.draw(millis()/1000d);
    
    background(0);
    noStroke();
    for (DomeCoord c : dome.coords){
        PVector p = dome.xyToScreen(dome.points.get(c));
        fill(dome.getColor(c));
           ellipse(p.x, p.y, 3, 3);
        }
    opc.draw();
    
    text("opc @" + opc.host, 100, height - 10);
    text(String.format("%.1ffps", frameRate), 10, height - 10);
    
}
