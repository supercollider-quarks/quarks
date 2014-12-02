
ReferenceClock {
	var clock, <>offset = 0;

	*new { arg tempo, beats;
		^super.new.init(tempo, beats)
	}

	elapsedBeats {
		^clock.elapsedBeats + offset
	}

	tempo {
		^clock.tempo
	}

	elapsedBeats_ { |beats|
		offset = beats - clock.elapsedBeats
	}

	tempo_ { |tempo|
		clock.tempo = tempo
	}

	adjust { arg tempo, beats;
		beats !? { offset = beats - clock.elapsedBeats };
		tempo !? { clock.tempo = tempo };
	}

	init { arg tempo, beats;
		var clockBeats;
		clock !? {
			clockBeats = clock.elapsedBeats;
			beats = beats ? clockBeats;
			tempo = tempo ? clock.tempo;
			clock.stop;
		};
		clock = TempoClock.new(tempo, beats).permanent_(true)
	}

	permanent { ^true }
}


TelepathicReferenceClock : ReferenceClock {

	var <>name;
	var responder;

	classvar <cmd = \refClockSet;

	*new { arg tempo, beats, name;
		^super.new(tempo, beats).name_(name ? \telepathicClock)
	}

	startListen {
		// cmd, name, tempo, beats, beatWrap
		responder = OSCresponderNode(nil, cmd, { |t, r, msg|
			var name, tempo, beats, beatWrap;
			if(msg[1] == name) {
				#tempo, beats, beatWrap = msg[2..];
				this.adjust(tempo, beats, beatWrap);
			}
		});
		responder.add;
	}

	stopListen {
		responder.remove;
	}
}


TelepathicClock : ListeningClock {

	var <>name = \telepathicClock, <responder;

	classvar <addClockSourceCmd = \addClockSource;


	addClockSource { |name|
		var clock;
		// allow other (local) clocks
		if(others.isNil or: { others.any { |clock| try { clock.name == name } ? false }.not }) {
			clock = TelepathicReferenceClock(this.tempo, this.elapsedBeats, name);
			clock.startListen;
			this.addClock(clock);
			weights = nil; // for now.
		}
	}

	removeClockSource { |name|
		var index, clock;
		if(others.isNil) { ^this };
		index = others.detectIndex { |clock| try { clock.name == name } ? false };
		if(index.notNil) {
			clock = others.removeAt(index);
			// todo: weights
			clock.stopListen;
		};
	}

	allowOthersToAdd { |flag = true|
		if(flag) {
			responder = OSCresponderNode(nil, addClockSourceCmd, { |t, r, msg, replyAddr|
				if(msg[1] == name) {
					if(msg[2] > 0) {
						this.addClockSource(msg[3])
					} {
						this.removeClockSource(msg[3])
					}
				}
			});
			responder.add;
		} {
			responder.remove;
		}
	}

	startListen {
		super.startListen;
		// cmd, name, flag (1= add, 0 = remove)
		others.do { |clock| try { clock.startListen } };
	}

	stopListen {
		super.stopListen;
		others.do { |clock| try { clock.stopListen } };
		this.allowOthersToAdd(false);
	}




}


+ TempoClock {

	initTeleport { |addr, name = \telepathicClock|
		addr.sendMsg(TelepathicClock.addClockSourceCmd, name, 1);
	}

	endTeleport { |addr, name = \telepathicClock|
		addr.sendMsg(TelepathicClock.addClockSourceCmd, name, 0);
	}

	teleport { |addr, name = \telepathicClock, beatWrap|
		addr.sendMsg(TelepathicReferenceClock.cmd, name, this.tempo, this.elapsedBeats, beatWrap)
	}

	clearQueue {
		queue.removeAllSuchThat(true);
	}
}


/*

// usage:

// local clock:
(
t.stop; x.stop;
t = TelepathicClock.new.permanent_(true);
t.empathy = 0.5;
t.confidence = 0.5;
t.phaseWrap = 4;
t.addClockSource(\test);

x = TelepathicClock.new.permanent_(true);
x.addClockSource(\test);
x.startListen;

t.verbose = true;
t.startListen;
n = NetAddr("127.0.0.1", 57120);
);

t.others.first.dump




(
SynthDef(\x, { |freq, sustain| Out.ar(0, XLine.kr(0.1, 0.0001, sustain, doneAction: 2) * SinOsc.ar(freq, 0.5pi)) }).add;
Pbind(\freq, 810, \sustain, 0.5, \dur, 1, \instrument, \x).play(t, quant:1);
Pbind(\freq, 8110, \sustain, 0.5, \dur, 1, \instrument, \x).play(x, quant:1);
Pbind(\freq, 2500, \sustain, 0.1, \dur, 1, \instrument, \x).play(TempoClock.default, quant:1);
);

(
TempoClock.default.tempo = rrand(1.0, 4.0);
TempoClock.default.teleport(n, \test, 4);
)


// end
t.endTeleport(n, 0);
t.stopListen;

t.stop;

*/

