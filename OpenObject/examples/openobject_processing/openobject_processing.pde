//first start supercollider and run the code in 'openobject_supercollider.html'

//requires the opensound control library by andreas schlegel
//oscP5 website at http://www.sojamo.de/oscP5

import netP5.*;
import oscP5.*;

NetAddress sc;
OscP5 osc;

void setup() {
  size(300, 300);
  osc= new OscP5(this, 12000);
  sc= new NetAddress("127.0.0.1", 57120);
}

void draw() {
  background(0);
  OscMessage msg= new OscMessage("/oo");
  msg.add("brussels");
  msg.add("set");
  msg.add("freq");
  msg.add(mouseX*10+5);
  msg.add("numharm");
  msg.add(mouseY/100+1);
  osc.send(msg, sc);
  noFill();
  stroke(123);
  ellipse(mouseX, mouseY, 80, 80);
  if(mousePressed) {
    ellipse(mouseX, mouseY, 30, 30);
  }
}

void mousePressed() {
  OscMessage msg= new OscMessage("/oo");
  msg.add("brussels");
  msg.add("play");
  osc.send(msg, sc);
}

void mouseReleased() {
  OscMessage msg= new OscMessage("/oo");
  msg.add("brussels");
  msg.add("stop");
  osc.send(msg, sc);
}
