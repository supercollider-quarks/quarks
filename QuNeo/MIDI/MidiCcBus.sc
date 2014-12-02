/*
MidiCcBus by Jonathan Siemasko - Email: schemawound@yahoo.com - Web: http://schemawound.com/

This control registers a function to update a bus from a specific CC message/Channel combo.  The bus will output the values 0-1.
Includes shotcuts for map, value and creating a Ugen from the bus
*/

MidiCcBus{
	var <cc, <channel, <scInPort, <scOutPort, <name;
	var <bus, displayDebugInfo, <>func;

	*new{|cc, channel = 0, scInPort, scOutPort, name|
		^super
			.newCopyArgs(cc, channel, scInPort, scOutPort, name)
			.init()
	}

	init{
		func = {};
		if(cc.notNil, {
			bus = Bus.control(Server.default, 1);
			//Set bus value.  Divide by 127 to normalize to 0..1
			MIDIdef.cc(name,
				{|val, num, chan, src|
					if (chan == channel, {
						bus.set(val/127);
						func.value(val/127, num, chan, src, name, displayDebugInfo);
						if(displayDebugInfo == true, {bus.get{|val|(name ++ ': ' ++ val).postln}});
					});
				},
				ccNum: cc,
				chan: channel
				//,srcID: scInPort
			).permanent = true;
		});
	}

	//Set to display debug values to the console
	debug{|setDebug = true| displayDebugInfo = setDebug;}

	//Returns a map for controlling a synth's node inputs
	map{^bus.asMap;}

	//Returns the current bus value
	value{^bus.getSynchronous;}

	//Returns an OutputProxy mapped to the bus to use it inside of SynthDefs
	//Range is normalized from 0 to 1
	ar{|mul=1, add=0, lagTime = 0.1|^MulAdd(Lag.ar(bus.ar(1), lagTime), mul, add);}
	kr{|mul=1, add=0, lagTime = 0.1|^MulAdd(Lag.kr(bus.kr(1), lagTime), mul, add);}
}