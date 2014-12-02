
// see chucking class MT

MTSocket : AbstractMIDISocket {
		// will look up mt based on channel index
	classvar	mtClass;
	
	var	destID;

		// this is to avoid a hard dependency on chucklib itself
		// since I inherit from AbstractMIDISocket, I have to live in the MIDI quark
		// obviously this won't work if you don't have chucklib
	*initClass {
		mtClass = 'MT'.asClass;
	}

	init {
		destID = parent.channel;
		destination = this;
	}

	noteOn { |num, vel|
		mtClass.new(destID).noteOn(num);
	}
	
	noteOff {}
	
	active { ^mtClass.new(destID).notNil and: { mtClass.new(destID).value.size > 0 } }
}