+ Buffer {
	*readDefault { |server, action, bufnum|
		^this.read( server, Platform.resourceDir +/+ "sounds/a11wlk01.wav", action: action, bufnum: bufnum );
	}
	
	*readDefault44_1 { |server, action, bufnum|
		^this.read( server, Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff", action: action, bufnum: bufnum );
		
	}
	
}