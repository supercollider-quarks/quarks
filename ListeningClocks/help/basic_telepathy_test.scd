/*
The plan is to reimplement the not-quite-correct TelepathicClock
as to match a future time sharing protocol for laptop ensembles.
TelepathicClock may change implementation.
ListeningClock and SoftClock will stay the same.
*/

// actually, we may not even need the following, because TempoClock beats can be set directly.

(
t = TempoClock.new;
fork { loop { 0.2.wait; t.elapsedBeats.postln } };
);

t.beats = 10;

(
t = TempoClock.new;
Pbind(\freq, \rest, \ggg, Pfunc { thisThread.clock.elapsedBeats.postln }).play(t);
)
t.beats = 10;


////////////////////////////////////////////
// but, this makes it explicit.

(
// basic test:
a = ReferenceClock.new(TempoClock.default.tempo, TempoClock.default.elapsedBeats);
b = ListeningClock.new(TempoClock.default.tempo, TempoClock.default.elapsedBeats);
b.permanent_(true);
b.addClock(a, 1);
b.startListen;
b.verbose = true;
);

// push reference into the future
a.adjust(1, b.elapsedBeats + 3);
// push reference into the past
a.adjust(1, b.elapsedBeats - 3);

// regular updates
fork { loop { 1.wait; a.adjust(1, b.elapsedBeats + 0.1.rand2) } }


(
SynthDef(\Pdefhelp, { arg out, freq, sustain=1, amp=1, pan;
	    var env = EnvGen.ar(Env.perc(0.01, sustain), 1, doneAction:2);
	    Out.ar(out, Pan2.ar(SinOsc.ar(freq, 0.5pi, amp * env), pan));
}).add;
)

(
Pbind(\instrument, \Pdefhelp, \dur, 0.5, \note, Pseq([0, 3, 6, 7], inf)).play(b, quant: 1);
Pbind(\instrument, \Pdefhelp, \dur, 0.5, \note, Pseq([0, 3, 6, 7] + 5, inf)).play(TempoClock.default, quant: 1);
fork { loop { 1.wait; a.adjust(TempoClock.default.tempo, TempoClock.default.elapsedBeats) } }
);

TempoClock.default.tempo = 1.4;
TempoClock.default.tempo = 0.4;
TempoClock.default.tempo = 1.0;
TempoClock.default.tempo = rrand(0.7, 1.2);


b.verbose = false;

Tdef(\x, { loop { 1.4.wait; TempoClock.default.tempo = TempoClock.default.tempo * rrand(0.94, 1.05) } }).play;
b.empathy = 1;
b.confidence = 0.2;

Tdef(\x, { loop { 1.4.wait; TempoClock.default.tempo = TempoClock.default.tempo * rrand(0.8, 1.2) } }).play;
Tdef(\x, { loop { 1.4.wait; TempoClock.default.tempo = TempoClock.default.tempo  } }).play;
TempoClock.default.tempo = 2;


b.stop;


///////// reference implementation

q = ();
q.is_master = false;
q.clock_port = 57120;
q.broadcast = NetAddr("255.255.255.255", q.clock_port);
q.master_addr;
q.clock_offset = 0;
q.cycle = 4;
q.current_clock = TempoClock.default;
q.be_master = {
	q.master_clock = q.current_clock;
	Routine {
		inf.do { |i|
			q.beat_n = i % 4; // shouldn't we maybe pick up from where we were?
			q.beat_d = 1;
			addr.sendMsg("/clock/stream",
				q.master_clock.elapsedBeats,
				q.master_clock.tempo * 60,
				q.beat_n,
				q.beat_d,
				q.cycle
			);
			(1/4).wait;
		}
	}.play(q.master_clock, quant: 1)
};




/* Part A */


OSCFunc({ |msg|
	var time, bpm, beat_n, beat_d, cycle;
	if(q.is_master) {
		# time, bpm, beat_n, beat_d, cycle = msg[1..];
		q.broadcast.sendMsg("/clock/bpm/update", time, bpm, beat_n, beat_d, cycle);
	};
}, "/clock/bpm/set", recvPort: q.clock_port);

OSCFunc({ |msg|
	var time, bpm, beat_n, beat_d, cycle;
	# time, bpm, beat_n, beat_d, cycle = msg[1..];
	if(q.is_master.not) {
		# time, bpm, beat_n, beat_d, cycle = msg[1..];
		q.master_addr = addr; // stream sets master addr
		q.current_clock.schedAbs(time,
			q.tempo = bpm * 60;
		};
	};
}, "/clock/bpm/update", recvPort: q.clock_port);

// receive clock stream
OSCFunc({ |msg, time, addr|
	var time, bpm, beat_n, beat_d, cycle;
	if(q.is_master.not) {
		# time, bpm, beat_n, beat_d, cycle = msg[1..];
		q.master_addr = addr; // stream sets master addr
		q.current_clock.schedAbs(time,
			q.current_clock.beats = beat_n / beat_d; // ?? maybe better use a reference clock for this.
			q.cycle = cycle;
			q.tempo = bpm * 60;
		};
	};
}, "/clock/stream", recvPort: q.clock_port);


OSCFunc({ |msg, time, addr|
	if(q.is_master) {
		addr.sendMsg("/clock/bpm/update", time, bpm, beat_n, beat_d, cycle);
	};
}, "/clock/bpm/q", recvPort: q.clock_port);

// find current master, in case one dosen't want to use stream

OSCFunc({ |msg,  time, addr|
	if(q.is_master) {
		addr.sendMsg("/clock/master/r");
	}
}, "/clock/master/q", recvPort: q.clock_port);

OSCFunc({ |msg|
	q.master_addr = addr;
}, "/clock/master/r", recvPort: q.clock_port);

// relative clock offset

OSCFunc({ |msg, time, addr|
	if(q.is_master) {
		addr.sendMsg("/clock/offset/r", q.master_clock.elapsedBeats);
	}
}, "/clock/offset/q", recvPort: q.clock_port);

OSCFunc({ |msg|
	q.clock_offset = msg[1];
}, "/clock/offset/r", recvPort: q.clock_port);


/* Part B */


OSCFunc({ |msg|
	var time1 = msg[1];
}, "/clock/sync/q", recvPort: q.clock_port);

OSCFunc({ |msg|
	var time1 = msg[1];
	var time2 = msg[2];
}, "/clock/sync/r", recvPort: q.clock_port);

OSCFunc({ |msg|
	var token = msg[1];
}, "/clock/election/q", recvPort: q.clock_port);

OSCFunc({ |msg|

}, "/clock/election/r", recvPort: q.clock_port);



























