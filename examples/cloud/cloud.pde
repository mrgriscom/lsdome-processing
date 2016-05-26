/*
 * Fractal noise animation. Modified version of Micah Scott's code at 
 * https://github.com/scanlime/fadecandy/tree/master/examples/processing/grid24x8z_clouds
 */
import me.lsdo.processing.*;

CloudsSketch cloud;
Dome dome;
OPC opc;
void setup() {
    size(300, 300);
    dome = new Dome(width);
    dome.init();
    opc = new OPC(this, "127.0.0.1", 7890);
    opc.setDome(dome);
    cloud = new CloudsSketch(this, dome, width);
  //cloud.init();
  colorMode(HSB,255);
}

void draw() {
  for (DomeCoord c : dome.coords){
      
      dome.setColor(c, cloud.samplePoint(dome.points.get(c), millis()/1000d, 0));
  }
  
      background(0);
    noStroke();
    for (DomeCoord c : dome.coords){
        PVector p = dome.xyToScreen(dome.points.get(c));
        fill(dome.getColor(c));
           ellipse(p.x, p.y, 3, 3);
        }
     opc.draw();
        
    fill(128);
    text("opc @" + opc.host, 100, height - 10);
    text(String.format("%.1ffps", frameRate), 10, height - 10);
  
}

void keyPressed(){
   //driver.processKeyInput();
}


