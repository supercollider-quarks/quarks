(
q = Namespace();

q.n = 15;

q.sources = [
	{
		var snd = RLPF.ar(Pulse.ar(\freq.kr(200), 0.2), 2500, 0.8) * 0.3;
		var env = EnvGen.kr(Env.asr, \gate.kr(1), doneAction: 2);
		Out.ar(0, snd * env);
	},
	{
		var snd = SinOsc.ar(\freq.kr(200)) * 0.3;
		var env = EnvGen.kr(Env.asr, \gate.kr(1), doneAction: 2);
		Out.ar(0, snd * env);
	},
	{
		var snd = Saw.ar(\freq.kr(200)) * 0.3;
		var env = EnvGen.kr(Env.asr, \gate.kr(1), doneAction: 2);
		Out.ar(0, snd * env);
	}
];

q.names = [\def1, \def2, \def3];

[q.sources, q.names].flopWith{ |func, name|
	SynthDef(name, func).add
};

q.synths = 15.collect{ [] };

//GUIS
q.mpdwin = Window("MPD18 use case (JNCv2)").front;
q.butvals = 0!4!4;

q.buts = 4.collect { |i|
	4.collect {|j|
		Button(q.mpdwin, Rect(i * 80 + 5, 240 - (j * 80) + 5, 75, 75))
		.states_([["up" + (i + 1 + (j * 4)), Color.black], ["DOWN", Color.black, Color.green]]);
	}
}.flop;

q.playButs = q.buts[0][..2];
q.memButs = q.buts[1..].flatten;

q.sl = Slider(q.mpdwin, Rect(340, 25, 40, 280));

q.shifter = q.buts [0][3];
q.shifter.states_([["shift", Color.black], ["SHIFT", Color.black, Color.green]]);

CmdPeriod.add({ q.mpdwin !? _.close });

q[\startSynths] = { |i, freqs, sources|
	IO{
		q.synths[i] = [sources[i], freqs].flopWith{ |j, freq|
			Synth(q.names[j], [\freq, freq.linlin(0.0,1.0,300,2000)])
		};
	}
};

q[\stopSynths] = { |i|
	IO{ q.synths[i].do(_.release); q.synths[i] = [] }
};

q[\setFreq] = { |i, freqs|
	IO{ [q.synths[i], freqs].flopWith{ |s,v| s.set(\freq,v.linlin(0.0,1.0,300,2000)) } }
};

q.softset = { |es, delta = 0.1|
	var outSig;
	var checked =  { |e|
		e.storePrevious(0.0).collect{ |t|
			var current = outSig.now;
			if( (absdif(current, t.at2) < delta) || (absdif(current,t.at2) < delta) ){Some(t.at2)}{None()}
		}.selectSome
	};
	var outES = es.collect(checked).mreduce;
	outSig = outES.hold(0.0);
	outES
};

//FRP
EventNetwork(ENDef({

	var freqArraySig; //declare now for recursive use

	//declare all the input event streams (and signals)
	var flatPads = q.buts.flat;
	var allButtonsESs = (flatPads[0..2]++flatPads[4..]).collect{ |x| x.enInES.collect(_.booleanValue) };

	var playButsESs = allButtonsESs[..2];
	var memButsESs = allButtonsESs[3..];

	var shiftES = q.shifter.enInES.collect(_.booleanValue); //mimics just press start
	var shiftSig = shiftES.hold(false);
	var shiftSigNot = shiftSig.collect(_.not);

	var sliderES = q.sl.enInES;
	var sliderSig = sliderES.hold(0);

	//copying logic
	var copyFromTo =
	//merge all button pressing events gated with shift
	//state is T([],Option Int)
    //state means copy settings from play buttons in array to membutton in Option.
	(shiftSig.when(
		(playButsESs.collect({ |es,i| es.select(I.d).collect{
			{|state|
				//already made assignment so clean state
				if(state.at2.isDefined) {
					T([i],None())
				}
				//collecting things to assign
				{
					if(state.at1.includes(i).not){
						T(state.at1++[i],None())
					}{
						state
					}
				}
		} } }) ++
		memButsESs.collect({ |es,i| es.select(I.d).collect{
			{|state|
				if(state.at2.isDefined) {
					state
				} {
					T(state.at1, Some(i+3) )
				}
	} } })).mreduce )
	| shiftES.collect{ { T([],None()) } })
	//run a statefull function on those events
	.injectF( T([],None()) )
	.collect{ |tup|
		tup.at2.collect{ |x| tup.at2_(x) }
	}
	.enDebug("state")
	//extract copyDescription
	//only let through when we have pushed two buttons in sequence
	.selectSome;
	//.enDebug("swap");
	//copying the settings causes pickup mode

	var sources = copyFromTo.collect{ |tup|
		{ |state| state[tup.at2] = tup.at1; state }
	}.injectFSig( (0..14).collect({ |x| [x.mod(3)] }) );
	//.enDebug("sources"); // [ EventStream [Int] ]

	//pad press logic
	var processOnePad = { |padES, shiftSigNot, sliderES, copyFromTo, sources, i|

		var padSig = padES.hold(false);
		var padWhenShiftOff = when( shiftSigNot, padES);

		var sliderWhenPadOnAndShiftOff = q.softset.( [when( (_&&_).lift.(shiftSigNot, padSig ) , sliderES )] );

		var copy = copyFromTo.select{ |t| t.at2 == i}.collect{ |t|
			var currentFreqs = freqArraySig.now.postln;
			currentFreqs[t.at1].collect(_.first).postln
		};

		var fromSlider = { |xs, v| v.dup(xs[i].size) } <%> sources <@> sliderWhenPadOnAndShiftOff;
		var freqs = copy | fromSlider;
		var freqsSig = freqs.hold([0.0]);

		//Perform actions
		//padSig.enDebug("pad "++i);
		//sliderWhenPadOnAndShiftOff.enDebug("sl "++i);
		//freqsSig.enDebug("freq "++i);

		//freq changes set new freq to synth
		freqs.collect{ |fs| q[\setFreq].(i,fs) }
		//.enDebug("freq "++i)
		.enOut;

		//pad on -> synth start
		({ |fs, sources, play| q[\startSynths].(i, fs, sources) } <%> freqsSig <*> sources <@> padWhenShiftOff.select(I.d) )
		//.enDebug("start "++i)
		.enOut;

		//pad off -> synth stop
		padES.select(_.not).collect{ q[\stopSynths].(i) }
		//.enDebug("stop "+i)
		.enOut;

		freqsSig
	};

	var freqSigs = [allButtonsESs, (1..q.n)-1].flopWith{ |pad, i|
		processOnePad.(pad, shiftSigNot, sliderES, copyFromTo, sources, i)
	};

	freqArraySig = freqSigs.sequence;

}),true ).start
)

q.synths
