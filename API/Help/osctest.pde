/**
 * basic send to SuperCollider
 * felix
 * requires osc library:
 * oscP5broadcastClient by andreas schlegel
 * oscP5 website at http://www.sojamo.de/oscP5
 */

import oscP5.*;
import netP5.*;

boolean wiggled = false;

OscP5 oscP5;

/* a NetAddress contains the ip address and port number of a remote location in the network. */
NetAddress sc; 

void setup() {
  size(400,400);
  frameRate(25);
  
  /* create a new instance of oscP5. 
   * 12000 is the port number processing is listening for incoming osc messages.
   */
   int listeningPort = 12000;
   oscP5 = new OscP5(this,listeningPort,OscP5.UDP);
  
  /* create a new NetAddress. a NetAddress is used when sending osc messages
   * with the oscP5.send method.
   */
  
  /* the address of SC over there */
  sc = new NetAddress("127.0.0.1",57120);

  // register so responses from commands sent from here are sent to my listening port
  OscMessage msg = new OscMessage("/API/registerListener");
  msg.add(listeningPort);
  // optional. default is /response
  // msg.add( "/customResponsePath" );
  oscP5.send(msg, sc);
}


void draw() {
  background(0);
  text("Click mouse to wiggle.",100,100);
  if(wiggled) {
    text("He wiggled.",100,130);   
    // I've got a brain the size of a planet and you want me to post this inane messages to the screen ?   
  }
}


void mousePressed() {

  /* create a new OscMessage with an address pattern, in this case /myApp/wiggle . */
  OscMessage msg = new OscMessage("/myApp/wiggle");
  /* add a value (an integer) to the OscMessage */
  msg.add(4);
  println("sending");
  oscP5.send(msg, sc);
}



/* incoming osc message are forwarded to the oscEvent method. */
void oscEvent(OscMessage msg) {
  /* get and print the address pattern and the typetag of the received OscMessage */
  println("### received an osc message with addrpattern "+msg.addrPattern()+" and typetag "+msg.typetag());
  msg.print();
  wiggled = true;
}
