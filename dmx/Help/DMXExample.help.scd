// This creates an instance of the DMX class (see the DMX quark):
d = DMX.new;
// create a DMX device (the argument is the serial path at which it can be reached; see SerialPort.devices)
e = EntTecDMXUSBPro.new( "/dev/ttyUSB0" );
// set the device for DMX
d.device = e;


// create an empty cue:
c = DMXCue.new;
// these are the work lights in one part of the room (DMX channels 98, 100, 102)
([98,100,102]-1).do{ |it| c.put( it, 1 ); };

d.currentCue = c;
d.setCue;

// create an empty cue for darkness:
b = DMXCue.new;

// b is darkness:
d.currentCue = b;
d.setCue;

// these are the 9 dmx channels for a ring of lights:
~dmxRing = [ 55, 52, 67, 68, 71, 72, 75, 61, 58 ];

// this is the subcue we'll use for the ring of lights
// subcues only set the channels that are explicitly set, not other channels
~dmxRingCue = DMXSubCue.new;

// this is the subcue we'll use for the light spot in the middle
~dmxSpot = DMXSubCue.new;

// these is one dmx channel for the spot:
~dmxSpotL = 87;

(
Tdef( \dmxtest, { loop{
	// set all channels in the ring to zero:
	~dmxRing.do{ |jt| ~dmxRingCue.put( jt, 0 ); };
	// set a random channel to 1
	~dmxRingCue.put( ~dmxRing.choose, 1 );
	// fade to the updated cue:
	d.fade( ~dmxRingCue, 0.5, 'linear', 0.1 );
	1.0.wait;
};
} );
);