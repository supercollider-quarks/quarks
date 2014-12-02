// wslib 2009
// a PrivateBus is an audio bus that is not connected to a hardware input or output
// all variations of the In/Out ugens are provided
// this only works on the Server side, and is not compatible with the Bus class
// using private buses in this manner only makes sense with user-assigned bus numbers
/* example:

(
SynthDef( "reverb", { |revIn = 0, out = 0, amp = 0.1, room = 0.5|
	var input;
	input = PrivateIn.ar( revIn, 1 ) * amp;
	Out.ar( out, FreeVerb2.ar( input, input, 1, room ) );
	}).send(s);
	
SynthDef( "pulse", { |revOut = 0, speed = 1, freq = 880, out = 0, revAmt = 1, amp = 0.1, pan = 0|
	var sig;
	sig = LFPulse.kr( speed, 0, 0.1 ) * LFPar.ar( freq, 0, amp );
	Out.ar( out, Pan2.ar( sig, pan ) );
	PrivateOut.ar( revOut, sig * revAmt );
	}).send(s);	
)

// two different reverbs
a = Synth.tail(s, "reverb", [ revIn: 0, room: 0.9 ] );
b = Synth.tail(s, "reverb", [ revIn: 1, room: 0.3, amp: 0.3 ] );

// two pulses, one sent to each reverb
c = Synth.head(s, "pulse", [ revOut: 0 ] );
d = Synth.head(s, "pulse", [ revOut: 1, speed: 0.79, freq: 1100 ] );

*/

FirstPrivateBus {
	*ir {  ^NumOutputBuses.ir + NumInputBuses.ir }
	}

PrivateIn {
	*busClass { ^In; }
	*ar { |bus=0, numChannels = 1|
		^this.busClass.ar( FirstPrivateBus.ir + bus, numChannels );
		}
	}

PrivateInFeedback : PrivateIn { *busClass { ^InFeedback; } }

PrivateOut {
	*busClass { ^Out; }
	*ar { |bus=0, channelsArray|
		^this.busClass.ar( FirstPrivateBus.ir + bus, channelsArray );
		}
	}
	
PrivateReplaceOut : PrivateOut { *busClass { ^ReplaceOut; } }
PrivateOffsetOut : PrivateOut { *busClass { ^OffsetOut; } }

PrivateXOut {
	*busClass { ^XOut; }
	*ar { |bus=0, xfade, channelsArray|
		^this.busClass.ar( FirstPrivateBus.ir + bus, xfade, channelsArray );
		}
	}

PrivatePanOut : PrivateXOut {  *busClass { ^PanOut; } }