/*
DMX framework for supercollider
(c) 2007-9 Marije Baalman (nescivi)
GNU/GPL v2.0 or later
*/

// for now, we assume that the DMXDevice shows up as a SerialPort in the computer; this is the case for the EntTec DMX USB Pro, which is a subclass of DMXDevice
// DMXDevice just encapsulates the common properties of DMXDevices

DMXDevice : SerialPort{

	sendDMX{ arg cue;
		var datablob;
		var cuesize = cue.size + 1;
		// Int8Array[0] is the DMX start code
		//		datablob = this.createSendHeader(cuesize) ++ Int8Array[0] ++ cue.asInt8 ++ this.createFooter;
        if ( cue.mode == \float ){
            datablob = this.createSendHeader(cuesize) ++ [0] ++ cue.asInt8 ++ this.createFooter;
        }{
            datablob = this.createSendHeader(cuesize) ++ [0] ++ cue ++ this.createFooter;
        };
		// add in when testing for real:
		Routine({ this.putAll( datablob );}).play
	}

	createSendHeader{ arg data_size=512;
		// subclass responsibility
		//	^Int8Array[];
		^[]
	}

	createFooter{
		// subclass responsibility
		//	^Int8Array[];
		^[]
	}
}