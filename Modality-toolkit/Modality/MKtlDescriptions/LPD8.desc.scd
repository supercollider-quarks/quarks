// AKAI professional LPD8 Laptop Pad controller specs

(
device: "LPD8",
protocol: \midi,
description: (
	\kn: Event.newFrom( (1..8).collect{ |midiNum|
		[ midiNum, (\midiMsgType: \cc, \type: \knob, \midiChan: 0, \midiNum: midiNum, \spec: \midiCC) ]
	}.flatten ),
	\pad: (
		\on: Event.newFrom( (36..43).collect{ |note|
			[
				note,
				(\midiMsgType: \noteOn, \type: \pad, \midiChan: 0, \midiNum:  note, \spec: \midiVel)
			]
		}.flatten ),
		\off: Event.newFrom( (36..43).collect{ |note|
			[
				note,
				(\midiMsgType: \noteOff, \type: \pad, \midiChan: 0, \midiNum:  note, \spec: \midiVel)
			]
		}.flatten )
	),
	/*
	//no pad cc because it uses the same cc numbers as knobs... dugh...
	\padCC: ((1..6)++[8,9]).collect{ |cc|
	(\midiMsgType: \cc, \type: \pad, \midiChan: 0, \midiNum:  cc, \spec: \midiCC)
	}*/
);
);

/*

MIDIMKtl.find; // find all MDIDI devices and list them
MIDIMKtl('lpd80'); // make a  MIDIMKtl for LPD8.

(
SynthDef(\test,{
	var x = (SinOsc.ar( \freq.kr(400) ) * \amp.kr(1) ).clip * 0.5 * EnvGen.ar(Env.adsr,\gate.kr(1));
	Out.ar(0,x)
}).add
)

(
x = ();
MIDIMKtl('lpd80').elements.padOn.do{ |el,i|
	el.action_({ |v| x.at(i) ?? _.free; x[i] = Synth(\test,[\amp, v.value*30, \freq, 400+(100*i)]) })
};
MIDIMKtl('lpd80').elements.padOff.do{ |el,i|
	el.action_({ |v| x.at(i).set(\gate,0); x[i] = nil })
};
)
*/